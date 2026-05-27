package com.example.upstream.payment;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.upstream.model.PaymentResult;
import org.springframework.stereotype.Component;

/**
 * 支付状态机模拟：
 *   POST 创建 → status=PENDING
 *   GET 查询  → 前 2 次返回 PENDING，第 3 次起返回 PAID
 *
 * 这迫使 order-service 实现轮询逻辑（filter + repeatWhenEmpty），是核心教学场景。
 */
@Component
public class PaymentStore {

    private record Entry(String paymentId, String orderId, double amount, AtomicInteger polls) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    private static final int POLLS_UNTIL_PAID = 3;

    public PaymentResult create(String orderId, double amount) {
        String id = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        store.put(id, new Entry(id, orderId, amount, new AtomicInteger(0)));
        return new PaymentResult(id, "PENDING");
    }

    public PaymentResult query(String paymentId) {
        Entry e = store.get(paymentId);
        if (e == null) return null;
        int n = e.polls().incrementAndGet();
        String status = n >= POLLS_UNTIL_PAID ? "PAID" : "PENDING";
        return new PaymentResult(e.paymentId(), status);
    }
}
