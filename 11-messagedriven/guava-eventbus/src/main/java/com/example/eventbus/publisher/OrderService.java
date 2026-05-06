package com.example.eventbus.publisher;

import com.example.eventbus.event.OrderPlacedEvent;
import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final EventBus eventBus;

    public OrderService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void placeOrder(String orderId, String userId, double totalAmount) {
        System.out.println("[OrderService] 下单成功: " + orderId);
        eventBus.post(new OrderPlacedEvent(orderId, userId, totalAmount));
    }
}
