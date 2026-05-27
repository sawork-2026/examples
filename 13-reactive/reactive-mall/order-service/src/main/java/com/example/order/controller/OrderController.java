package com.example.order.controller;

import java.util.Map;

import com.example.order.model.Order;
import com.example.order.model.OrderEvent;
import com.example.order.model.OrderRequest;
import com.example.order.service.OrderEventPublisher;
import com.example.order.service.OrderOrchestrator;
import com.example.order.store.OrderStore;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderOrchestrator orchestrator;
    private final OrderStore orderStore;
    private final OrderEventPublisher eventPublisher;

    public OrderController(OrderOrchestrator orchestrator,
                           OrderStore orderStore,
                           OrderEventPublisher eventPublisher) {
        this.orchestrator = orchestrator;
        this.orderStore = orderStore;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 下单——返回 Mono<Order>，WebFlux 框架 subscribe 后整条 Reactor 链开始执行。
     */
    @PostMapping
    public Mono<Order> placeOrder(@RequestBody OrderRequest req) {
        return orchestrator.placeOrder(req);
    }

    /**
     * 查询订单状态。
     */
    @GetMapping("/{orderId}")
    public Mono<Object> getOrder(@PathVariable String orderId) {
        Order order = orderStore.find(orderId);
        if (order == null) {
            return Mono.just(Map.of("error", "Order not found: " + orderId));
        }
        return Mono.just(order);
    }

    /**
     * SSE 端点——实时推送订单生命周期事件。
     *
     * 用法：先在一个终端 curl -N http://localhost:8080/api/orders/{id}/events
     *       再在另一个终端 POST 下单，观察事件依次到达。
     *
     * produces = TEXT_EVENT_STREAM_VALUE 告诉浏览器/curl 这是 SSE 流。
     */
    @GetMapping(value = "/{orderId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OrderEvent>> orderEvents(@PathVariable String orderId) {
        return eventPublisher.stream(orderId);
    }
}
