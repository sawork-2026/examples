package com.example.order.model;

public record PaymentRequest(String orderId, double amount) {}
