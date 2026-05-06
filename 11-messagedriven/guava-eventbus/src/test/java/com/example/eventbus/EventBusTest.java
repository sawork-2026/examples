package com.example.eventbus;

import com.example.eventbus.event.OrderPlacedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    @Test
    void postEvent_allSubscribersReceive() {
        EventBus eventBus = new EventBus();
        List<String> received = new ArrayList<>();

        eventBus.register(new Object() {
            @Subscribe
            public void onOrder(OrderPlacedEvent e) {
                received.add("inventory:" + e.orderId());
            }
        });
        eventBus.register(new Object() {
            @Subscribe
            public void onOrder(OrderPlacedEvent e) {
                received.add("payment:" + e.orderId());
            }
        });
        eventBus.register(new Object() {
            @Subscribe
            public void onOrder(OrderPlacedEvent e) {
                received.add("notification:" + e.orderId());
            }
        });

        eventBus.post(new OrderPlacedEvent("ORD-001", "user-1", 100.0));

        assertEquals(3, received.size());
    }

    @Test
    void noSubscriber_noException() {
        EventBus eventBus = new EventBus();
        eventBus.post(new OrderPlacedEvent("ORD-002", "user-2", 200.0));
    }
}
