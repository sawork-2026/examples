package com.example.saga.model;

public record OrderRequest(
        String orderId,
        String productId,
        int qty,
        String userId
) {}
