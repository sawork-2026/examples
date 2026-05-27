package com.example.order.service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.example.order.client.InventoryClient;
import com.example.order.client.PaymentClient;
import com.example.order.client.ShipmentClient;
import com.example.order.exception.OutOfStockException;
import com.example.order.exception.PaymentTimeoutException;
import com.example.order.model.*;
import com.example.order.store.OrderStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * ★ 核心教学文件 ★
 *
 * 用 Reactor 算子编排下单全流程——5 个异步步骤串成一条链：
 *
 *   ① 检查库存 ──flatMap──▶ ② 创建支付 ──flatMap──▶ ③ 轮询等待支付确认
 *         ──flatMap──▶ ④ 扣减库存 ──flatMap──▶ ⑤ 创建物流 ──map──▶ 返回 Order
 *
 * 教学要点（对应 slides）：
 *   - flatMap 链：每一步依赖上一步的结果，串行异步编排
 *   - filter + repeatWhenEmpty：响应式轮询（替代 while+sleep 的命令式轮询）
 *   - timeout：超时保护，避免无限等待
 *   - switchIfEmpty：空结果处理（库存不足）
 *   - onErrorResume：按异常类型分别降级
 *   - doOnNext：在链中插入副作用（发 SSE 事件），不改变数据流
 *
 * 整条链在 subscribe 之前只是一份蓝图——声明式。
 * 当 Controller 返回这个 Mono<Order> 时，WebFlux 框架来 subscribe，数据才开始流动。
 */
@Component
public class OrderOrchestrator {

    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;
    private final ShipmentClient shipmentClient;
    private final OrderStore orderStore;
    private final OrderEventPublisher events;

    public OrderOrchestrator(InventoryClient inventoryClient,
                             PaymentClient paymentClient,
                             ShipmentClient shipmentClient,
                             OrderStore orderStore,
                             OrderEventPublisher events) {
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
        this.shipmentClient = shipmentClient;
        this.orderStore = orderStore;
        this.events = events;
    }

    public Mono<Order> placeOrder(OrderRequest req) {
        String orderId = UUID.randomUUID().toString().substring(0, 8);

        // 用 AtomicReference 跨 flatMap 步骤传递 paymentId
        // （每个 flatMap 变换了类型，paymentId 需要在后续步骤中使用）
        AtomicReference<String> paymentIdRef = new AtomicReference<>();

        // ─── 步骤 ① 检查库存 ───────────────────────────────────────────
        // WebClient GET → Mono<InventoryInfo>
        // filter: 库存 >= 需求量才放行
        // switchIfEmpty: filter 过滤掉后变成空 Mono → 抛 OutOfStockException
        return inventoryClient.checkStock(req.productId())
                .filter(inv -> inv.stock() >= req.quantity())
                .switchIfEmpty(Mono.error(new OutOfStockException(req.productId())))
                .doOnNext(inv -> events.emit(
                        new OrderEvent(orderId, "INVENTORY_CHECKED", "OK",
                                "stock=" + inv.stock())))

        // ─── 步骤 ② 创建支付单 ──────────────────────────────────────────
        // flatMap: 拿到库存结果后，异步调用支付服务创建支付单
        // 支付服务返回 status=PENDING
                .flatMap(inv -> paymentClient.createPayment(
                        new PaymentRequest(orderId, req.price() * req.quantity())))
                .doOnNext(pay -> {
                    paymentIdRef.set(pay.paymentId());
                    events.emit(new OrderEvent(orderId, "PAYMENT_CREATED", pay.status(),
                            pay.paymentId()));
                })

        // ─── 步骤 ③ 轮询等待支付确认 ─────────────────────────────────────
        // 这是最复杂也是最有教学价值的一段：
        //   1. queryPayment → Mono<PaymentResult>
        //   2. filter(PAID) → 如果还是 PENDING，filter 让 Mono 变空
        //   3. repeatWhenEmpty → 空 Mono 触发重新订阅（= 重新查询）
        //      delayElements(500ms) 控制轮询间隔，take(10) 限制最多重试 10 次
        //   4. timeout(8s) → 总超时保护
        //   5. switchIfEmpty → 10 次轮询后仍未 PAID → 抛 PaymentTimeoutException
        //
        // 对比命令式写法：
        //   while (true) { result = query(); if (PAID) break; Thread.sleep(500); }
        // Reactor 写法不阻塞任何线程——等待期间线程被释放去处理其他请求。
                .flatMap(pay -> paymentClient.queryPayment(pay.paymentId())
                        .filter(p -> "PAID".equals(p.status()))
                        .repeatWhenEmpty(repeat -> repeat
                                .delayElements(Duration.ofMillis(500))
                                .take(10))
                        .timeout(Duration.ofSeconds(8))
                        .switchIfEmpty(Mono.error(
                                new PaymentTimeoutException(paymentIdRef.get()))))
                .doOnNext(pay -> events.emit(
                        new OrderEvent(orderId, "PAYMENT_CONFIRMED", "PAID",
                                pay.paymentId())))

        // ─── 步骤 ④ 扣减库存 ─────────────────────────────────────────────
        // thenReturn(pay): deductStock 返回 Mono<Void>，
        // 用 thenReturn 把上游的 pay 透传下去，保持链的连续性
                .flatMap(pay -> inventoryClient.deductStock(
                                new DeductRequest(req.productId(), req.quantity()))
                        .thenReturn(pay))
                .doOnNext(pay -> events.emit(
                        new OrderEvent(orderId, "INVENTORY_DEDUCTED", "OK", "")))

        // ─── 步骤 ⑤ 创建物流单 ───────────────────────────────────────────
                .flatMap(pay -> shipmentClient.createShipment(
                        new ShipmentRequest(orderId, req.address())))
                .doOnNext(ship -> events.emit(
                        new OrderEvent(orderId, "SHIPMENT_CREATED", "OK",
                                ship.shipmentId())))

        // ─── 组装最终 Order ──────────────────────────────────────────────
                .map(ship -> new Order(orderId, req.productId(), req.quantity(),
                        req.price() * req.quantity(), "COMPLETED",
                        paymentIdRef.get(), ship.shipmentId()))
                .doOnNext(order -> {
                    orderStore.save(order);
                    events.emit(new OrderEvent(orderId, "ORDER_COMPLETED", "COMPLETED", ""));
                })

        // ─── 错误处理：按异常类型分别降级 ─────────────────────────────────
        // onErrorResume 可以链式叠加，每个只捕获特定异常类型
                .onErrorResume(OutOfStockException.class, e -> {
                    events.emit(new OrderEvent(orderId, "FAILED", "OUT_OF_STOCK",
                            e.getMessage()));
                    return Mono.just(failedOrder(orderId, req, "FAILED_OUT_OF_STOCK"));
                })
                .onErrorResume(PaymentTimeoutException.class, e -> {
                    events.emit(new OrderEvent(orderId, "FAILED", "PAYMENT_TIMEOUT",
                            e.getMessage()));
                    return Mono.just(failedOrder(orderId, req, "FAILED_PAYMENT_TIMEOUT"));
                })
                .onErrorResume(e -> {
                    events.emit(new OrderEvent(orderId, "FAILED", "ERROR", e.getMessage()));
                    return Mono.just(failedOrder(orderId, req, "FAILED_ERROR"));
                });
    }

    private Order failedOrder(String orderId, OrderRequest req, String status) {
        Order order = new Order(orderId, req.productId(), req.quantity(), 0, status, null, null);
        orderStore.save(order);
        return order;
    }
}
