package com.example.pipeline.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/** In-memory product catalog simulating a BaaS data store (Firebase / DynamoDB). */
@Service
public class ProductCatalog {

    private record ProductInfo(String name, double price, String category) {}

    private static final Map<String, ProductInfo> CATALOG = Map.of(
            "DOG-001",  new ProductInfo("Dalmatian",        150.00, "Dogs"),
            "DOG-002",  new ProductInfo("Golden Retriever", 200.00, "Dogs"),
            "CAT-001",  new ProductInfo("Persian Cat",      120.00, "Cats"),
            "FISH-001", new ProductInfo("Angel Fish",        15.00, "Fish"),
            "BIRD-001", new ProductInfo("Amazon Parrot",    250.00, "Birds")
    );

    /** Valid coupon codes and their discount rates. */
    private static final Map<String, Double> COUPONS = Map.of(
            "SAVE10", 0.10,
            "SAVE20", 0.20,
            "VIP",    0.15
    );

    public Optional<ProductInfo> findById(String productId) {
        return Optional.ofNullable(CATALOG.get(productId));
    }

    public String nameOf(String productId)     { return CATALOG.get(productId).name(); }
    public double priceOf(String productId)    { return CATALOG.get(productId).price(); }
    public String categoryOf(String productId) { return CATALOG.get(productId).category(); }
    public boolean exists(String productId)    { return CATALOG.containsKey(productId); }

    /** Returns the discount rate for a coupon code, or 0.0 if invalid. */
    public double couponDiscount(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) return 0.0;
        return COUPONS.getOrDefault(couponCode.toUpperCase(), 0.0);
    }
}
