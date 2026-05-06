package com.example.eventbus.event;

public record OrderPlacedEvent(String orderId, String userId, double totalAmount) {
}
