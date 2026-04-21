package com.example.serverless.service;

import com.example.serverless.model.OrderResult;
import com.example.serverless.model.PurchaseRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processes purchase orders with idempotency guarantee.
 *
 * FaaS platforms deliver events "at-least-once": a Lambda can be retried after a timeout
 * even if the first execution succeeded. The idempotency key ensures the caller always
 * gets the same OrderResult regardless of how many times the function is invoked.
 *
 * Production note: the map should live in Redis or DynamoDB (with a TTL), not in-process,
 * because each Lambda instance is independent and has no shared memory.
 */
@Service
public class OrderService {

    private final ConcurrentHashMap<String, OrderResult> processedOrders = new ConcurrentHashMap<>();
    private final ProductRepository productRepository;

    public OrderService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public OrderResult processOrder(PurchaseRequest request) {
        if (request.idempotencyKey() == null || request.idempotencyKey().isBlank()) {
            return new OrderResult(null, "REJECTED", "idempotencyKey is required");
        }

        // Return the cached result if this key was already processed (handles retries)
        OrderResult cached = processedOrders.get(request.idempotencyKey());
        if (cached != null) {
            return cached;
        }

        if (!productRepository.exists(request.productId())) {
            return new OrderResult(null, "REJECTED", "Product not found: " + request.productId());
        }

        if (request.qty() <= 0) {
            return new OrderResult(null, "REJECTED", "qty must be positive");
        }

        String orderId = UUID.randomUUID().toString();
        OrderResult result = new OrderResult(
                orderId,
                "SUCCESS",
                "Order placed: " + request.qty() + "x " + request.productId() + " for user " + request.userId()
        );

        processedOrders.put(request.idempotencyKey(), result);
        return result;
    }
}
