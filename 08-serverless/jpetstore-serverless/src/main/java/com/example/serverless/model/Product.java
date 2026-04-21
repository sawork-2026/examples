package com.example.serverless.model;

public record Product(
        String id,
        String name,
        String description,
        String category,
        double price
) {}
