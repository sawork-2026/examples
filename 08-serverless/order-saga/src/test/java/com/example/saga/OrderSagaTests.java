package com.example.saga;

import com.example.saga.model.OrderRequest;
import com.example.saga.model.SagaResult;
import com.example.saga.orchestrator.LocalSagaOrchestrator;
import com.example.saga.service.InventoryService;
import com.example.saga.service.NotificationService;
import com.example.saga.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderSagaTests {

    @Autowired private LocalSagaOrchestrator orchestrator;
    @Autowired private InventoryService inventory;
    @Autowired private PaymentService payment;
    @Autowired private NotificationService notification;

    @BeforeEach
    void setUp() {
        payment.clearDeclined("declined-user");
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void happyPath_allStepsSucceed() {
        int stockBefore = inventory.getStock("DOG-001");
        OrderRequest req = new OrderRequest("order-001", "DOG-001", 2, "user-001");

        SagaResult result = orchestrator.execute(req);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.transactionId()).startsWith("TX-");
        assertThat(inventory.getStock("DOG-001")).isEqualTo(stockBefore - 2);
        assertThat(notification.getSent()).anyMatch(m -> m.contains("order-001"));
    }

    // ── Compensation: payment failure ─────────────────────────────────────────

    @Test
    void paymentFailure_inventoryIsRolledBack() {
        // Given: user whose payment is declined
        payment.configureDeclined("declined-user");
        int stockBefore = inventory.getStock("CAT-001");

        OrderRequest req = new OrderRequest("order-002", "CAT-001", 1, "declined-user");
        SagaResult result = orchestrator.execute(req);

        // Saga fails
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).contains("Payment failed");

        // Compensation ran: inventory restored to original level
        assertThat(inventory.getStock("CAT-001"))
                .as("Inventory must be restored after payment failure")
                .isEqualTo(stockBefore);
    }

    // ── Compensation: insufficient stock ─────────────────────────────────────

    @Test
    void insufficientStock_sagaFailsAtStep1_nothingToCompensate() {
        int stockBefore = inventory.getStock("BIRD-001"); // only 3 in stock
        OrderRequest req = new OrderRequest("order-003", "BIRD-001", 99, "user-002");

        SagaResult result = orchestrator.execute(req);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).contains("Insufficient stock");

        // Stock unchanged — compensation was not needed
        assertThat(inventory.getStock("BIRD-001")).isEqualTo(stockBefore);
    }

    // ── Step 1 failure: unknown product ──────────────────────────────────────

    @Test
    void unknownProduct_sagaFailsAtStep1() {
        OrderRequest req = new OrderRequest("order-004", "UNKNOWN-999", 1, "user-003");
        SagaResult result = orchestrator.execute(req);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.message()).contains("Product not found");
    }

    // ── Idempotency of compensation ───────────────────────────────────────────

    @Test
    void twoSeparateOrders_stockDeductedCorrectly() {
        int stockBefore = inventory.getStock("FISH-001");

        orchestrator.execute(new OrderRequest("order-005", "FISH-001", 3, "user-004"));
        orchestrator.execute(new OrderRequest("order-006", "FISH-001", 5, "user-005"));

        assertThat(inventory.getStock("FISH-001")).isEqualTo(stockBefore - 8);
    }
}
