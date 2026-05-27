package com.example.order.store;

import java.util.concurrent.ConcurrentHashMap;

import com.example.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderStore {

    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();

    public void save(Order order) {
        store.put(order.orderId(), order);
    }

    public Order find(String orderId) {
        return store.get(orderId);
    }
}
