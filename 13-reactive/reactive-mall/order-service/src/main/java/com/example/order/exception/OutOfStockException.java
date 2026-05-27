package com.example.order.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productId) {
        super("Out of stock: " + productId);
    }
}
