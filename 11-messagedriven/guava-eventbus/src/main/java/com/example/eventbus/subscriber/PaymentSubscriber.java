package com.example.eventbus.subscriber;

import com.example.eventbus.event.OrderPlacedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class PaymentSubscriber {

    private final EventBus eventBus;

    public PaymentSubscriber(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.printf("[PaymentSubscriber] 发起支付 ¥%.2f，订单: %s%n",
                event.totalAmount(), event.orderId());
    }
}
