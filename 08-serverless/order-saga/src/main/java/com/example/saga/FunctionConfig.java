package com.example.saga;

import com.example.saga.model.SagaContext;
import com.example.saga.model.SagaResult;
import com.example.saga.service.InventoryService;
import com.example.saga.service.NotificationService;
import com.example.saga.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Defines each saga step as an independent Spring Cloud Function bean.
 *
 * In AWS Step Functions each bean is a separate Lambda function referenced by ARN
 * in step-functions-definition.json. The state machine passes SagaContext (as JSON)
 * from one Lambda's output to the next Lambda's input — exactly what we model here
 * by passing SagaContext between functions.
 *
 * Compensation functions (rollbackInventory) are also registered as regular Lambda
 * functions; Step Functions invokes them on the Catch branch of the state machine.
 */
@Configuration
public class FunctionConfig {

    /**
     * Step 1 — Deduct stock and price the order.
     * On failure: nothing to compensate (inventory was not changed).
     */
    @Bean
    public Function<SagaContext, SagaContext> deductInventory(InventoryService inventory) {
        return ctx -> {
            double unitPrice = inventory.deduct(ctx.getProductId(), ctx.getQty());
            return ctx.withUnitPrice(unitPrice).withInventoryDeducted();
        };
    }

    /**
     * Step 2 — Charge the user.
     * On failure: caller (orchestrator / Step Functions Catch) must invoke rollbackInventory.
     */
    @Bean
    public Function<SagaContext, SagaContext> chargePayment(PaymentService payment) {
        return ctx -> {
            String txId = payment.charge(ctx.getUserId(), ctx.getTotalAmount());
            return ctx.withPaymentTransactionId(txId);
        };
    }

    /**
     * Step 3 — Send order confirmation (best-effort).
     * Failure here does NOT roll back — notification is idempotent and retriable.
     */
    @Bean
    public Function<SagaContext, SagaResult> sendNotification(NotificationService notification) {
        return ctx -> {
            notification.sendOrderConfirmation(
                    ctx.getUserId(), ctx.getOrderId(), ctx.getPaymentTransactionId());
            return SagaResult.success(ctx.getOrderId(), ctx.getPaymentTransactionId());
        };
    }

    /**
     * Compensation — Restore stock when chargePayment fails.
     * In Step Functions this is the Task in the Catch → RollbackInventory state.
     */
    @Bean
    public Function<SagaContext, SagaContext> rollbackInventory(InventoryService inventory) {
        return ctx -> {
            if (ctx.isInventoryDeducted()) {
                inventory.restore(ctx.getProductId(), ctx.getQty());
            }
            return ctx;
        };
    }
}
