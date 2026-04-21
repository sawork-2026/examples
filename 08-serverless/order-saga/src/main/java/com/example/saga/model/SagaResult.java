package com.example.saga.model;

public record SagaResult(
        String orderId,
        String status,           // SUCCESS | FAILED
        String transactionId,    // payment transaction ID on success
        String message
) {
    public static SagaResult success(String orderId, String transactionId) {
        return new SagaResult(orderId, "SUCCESS", transactionId,
                "Order placed and payment charged. Transaction: " + transactionId);
    }

    public static SagaResult failed(String orderId, String reason) {
        return new SagaResult(orderId, "FAILED", null, reason);
    }

    public boolean isSuccess() { return "SUCCESS".equals(status); }
}
