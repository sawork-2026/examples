package com.example.order.model;

public record OrderRequest(String productId, int quantity, double price, String address) {}
