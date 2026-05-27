package com.example.order.model;

public record OrderEvent(String orderId, String step, String status, String detail) {}
