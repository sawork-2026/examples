package com.example.upstream.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InventoryStore {

    private final ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        stock.put("P001", 100);    // 充足库存
        stock.put("P002", 50);
        stock.put("P003", 0);     // 零库存——用于测试 switchIfEmpty / OutOfStockException
    }

    public int getStock(String productId) {
        return stock.getOrDefault(productId, -1);
    }

    public boolean deduct(String productId, int quantity) {
        return stock.computeIfPresent(productId, (k, v) -> v >= quantity ? v - quantity : v)
                != null && stock.getOrDefault(productId, 0) >= 0;
    }

    public Map<String, Integer> all() {
        return Map.copyOf(stock);
    }
}
