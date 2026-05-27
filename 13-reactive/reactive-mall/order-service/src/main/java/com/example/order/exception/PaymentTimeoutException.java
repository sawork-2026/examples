package com.example.order.exception;

public class PaymentTimeoutException extends RuntimeException {
    public PaymentTimeoutException(String paymentId) {
        super("Payment timeout: " + paymentId);
    }
}
