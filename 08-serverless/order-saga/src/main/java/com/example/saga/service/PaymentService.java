package com.example.saga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // User IDs that simulate declined cards — useful for testing compensation
    private final Set<String> declinedUsers = ConcurrentHashMap.newKeySet();
    private final Set<String> refundedTransactions = ConcurrentHashMap.newKeySet();

    /**
     * Charges the user for the given amount.
     *
     * @throws PaymentDeclinedException if the user's payment is configured to fail
     */
    public String charge(String userId, double amount) {
        if (declinedUsers.contains(userId)) {
            throw new PaymentDeclinedException("Payment declined for user: " + userId);
        }
        String transactionId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Payment charged: userId={}, amount={}, txId={}", userId, amount, transactionId);
        return transactionId;
    }

    /** Compensation: refunds the transaction (called when a downstream step fails). */
    public void refund(String transactionId) {
        refundedTransactions.add(transactionId);
        log.info("Payment refunded: txId={}", transactionId);
    }

    /** Test helper: configure a user whose payments should be declined. */
    public void configureDeclined(String userId)  { declinedUsers.add(userId); }
    public void clearDeclined(String userId)      { declinedUsers.remove(userId); }
    public boolean isRefunded(String txId)        { return refundedTransactions.contains(txId); }

    public static class PaymentDeclinedException extends RuntimeException {
        public PaymentDeclinedException(String msg) { super(msg); }
    }
}
