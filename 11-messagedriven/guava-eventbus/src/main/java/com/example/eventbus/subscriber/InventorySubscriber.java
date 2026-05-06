package com.example.eventbus.subscriber;

import com.example.eventbus.event.OrderPlacedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InventorySubscriber {

    private final EventBus eventBus;

    public InventorySubscriber(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[InventorySubscriber] 扣减库存，订单: " + event.orderId());
    }
}
