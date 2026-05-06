package com.example.amqp.event;

public record OrderPlacedEvent(String orderId, String userId, double totalAmount) {
}
