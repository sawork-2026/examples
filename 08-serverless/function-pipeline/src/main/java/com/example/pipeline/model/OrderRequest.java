package com.example.pipeline.model;

public record OrderRequest(String productId, int qty, String userId, String couponCode) {}
