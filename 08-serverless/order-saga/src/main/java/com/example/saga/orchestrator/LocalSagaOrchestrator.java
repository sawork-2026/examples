package com.example.saga.orchestrator;

import com.example.saga.model.OrderRequest;
import com.example.saga.model.SagaContext;
import com.example.saga.model.SagaResult;
import com.example.saga.service.InventoryService;
import com.example.saga.service.NotificationService;
import com.example.saga.service.PaymentService;
import com.example.saga.service.PaymentService.PaymentDeclinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Simulates AWS Step Functions locally.
 *
 * In production each step is a separate Lambda function wired together by a
 * Step Functions state machine (see step-functions-definition.json).
 * Here, the same function beans from FunctionCatalog are called in sequence
 * by this orchestrator, making the saga runnable without any cloud dependency.
 *
 * Flow:
 *   deductInventory → chargePayment → sendNotification → SUCCESS
 *                         ↑ fail
 *                   rollbackInventory → FAILED
 *
 * Compensation rule: only compensate steps that already succeeded.
 *   - If chargePayment fails  → rollback inventory (already deducted)
 *   - If sendNotification fails → order still succeeds (notification is best-effort)
 */
@Component
public class LocalSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(LocalSagaOrchestrator.class);

    private final Function<SagaContext, SagaContext> deductInventory;
    private final Function<SagaContext, SagaContext> chargePayment;
    private final Function<SagaContext, SagaResult>  sendNotification;
    private final Function<SagaContext, SagaContext> rollbackInventory;

    @SuppressWarnings("unchecked")
    public LocalSagaOrchestrator(FunctionCatalog catalog) {
        this.deductInventory  = catalog.lookup("deductInventory");
        this.chargePayment    = catalog.lookup("chargePayment");
        this.sendNotification = catalog.lookup("sendNotification");
        this.rollbackInventory = catalog.lookup("rollbackInventory");
    }

    public SagaResult execute(OrderRequest request) {
        log.info("=== Saga START: orderId={} ===", request.orderId());
        SagaContext ctx = SagaContext.from(request);

        // Step 1: deduct inventory
        try {
            ctx = deductInventory.apply(ctx);
        } catch (Exception e) {
            log.error("Step 1 FAILED (deductInventory): {}", e.getMessage());
            return SagaResult.failed(request.orderId(), "Inventory error: " + e.getMessage());
        }

        // Step 2: charge payment  ← failure here triggers inventory compensation
        try {
            ctx = chargePayment.apply(ctx);
        } catch (Exception e) {
            log.error("Step 2 FAILED (chargePayment): {} — compensating inventory", e.getMessage());
            rollbackInventory.apply(ctx);
            return SagaResult.failed(request.orderId(), "Payment failed: " + e.getMessage());
        }

        // Step 3: send notification  ← best-effort; failure does NOT roll back order
        try {
            SagaResult result = sendNotification.apply(ctx);
            log.info("=== Saga SUCCESS: orderId={} ===", request.orderId());
            return result;
        } catch (Exception e) {
            log.warn("Step 3 FAILED (sendNotification) — order still succeeded: {}", e.getMessage());
            return SagaResult.success(request.orderId(), ctx.getPaymentTransactionId());
        }
    }
}
