package com.example.eventbus.subscriber;

import com.example.eventbus.event.OrderPlacedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriber {

    private final EventBus eventBus;

    public NotificationSubscriber(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onOrderPlaced(OrderPlacedEvent event) {
        System.out.println("[NotificationSubscriber] 发送通知给用户: " + event.userId());
    }
}
