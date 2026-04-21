package com.example.serverless.model;

public record PurchaseRequest(
        String productId,
        int qty,
        String userId,
        String idempotencyKey  // caller-generated key; guarantees at-most-once processing on retry
) {}
