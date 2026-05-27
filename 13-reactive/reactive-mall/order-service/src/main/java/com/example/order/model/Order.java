package com.example.order.model;

public record Order(
        String orderId,
        String productId,
        int quantity,
        double totalPrice,
        String status,
        String paymentId,
        String shipmentId
) {}
