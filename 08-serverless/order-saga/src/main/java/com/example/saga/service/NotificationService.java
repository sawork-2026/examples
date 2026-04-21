package com.example.saga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // In-memory log; in production this would be SNS, SES, etc.
    private final List<String> sent = Collections.synchronizedList(new ArrayList<>());

    public void sendOrderConfirmation(String userId, String orderId, String transactionId) {
        String message = String.format(
                "Order %s confirmed for user %s. Payment transaction: %s", orderId, userId, transactionId);
        sent.add(message);
        log.info("Notification sent: {}", message);
    }

    public List<String> getSent() { return List.copyOf(sent); }
}
