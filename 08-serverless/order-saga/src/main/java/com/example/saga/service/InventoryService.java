package com.example.saga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    // product → available stock
    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "DOG-001", 10,
            "CAT-001", 5,
            "FISH-001", 50,
            "BIRD-001", 3
    ));

    /**
     * Attempts to deduct qty from stock.
     *
     * @throws IllegalArgumentException if product not found
     * @throws IllegalStateException    if stock is insufficient
     */
    public double deduct(String productId, int qty) {
        Integer current = stock.get(productId);
        if (current == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        if (current < qty) {
            throw new IllegalStateException(
                    "Insufficient stock for " + productId + ": requested=" + qty + " available=" + current);
        }
        stock.put(productId, current - qty);
        double unitPrice = priceOf(productId);
        log.info("Inventory deducted: {} x{}, remaining={}", productId, qty, current - qty);
        return unitPrice;
    }

    /** Compensation: restores qty to stock (called when a downstream step fails). */
    public void restore(String productId, int qty) {
        stock.merge(productId, qty, Integer::sum);
        log.info("Inventory restored: {} x{}, current={}", productId, qty, stock.get(productId));
    }

    public int getStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    private static double priceOf(String productId) {
        return switch (productId) {
            case "DOG-001"  -> 150.00;
            case "CAT-001"  -> 120.00;
            case "FISH-001" -> 15.00;
            case "BIRD-001" -> 250.00;
            default         -> 0.0;
        };
    }
}
