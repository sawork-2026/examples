package com.example.saga.model;

/**
 * Carries the full state of a saga execution across steps.
 *
 * In AWS Step Functions this is the JSON payload passed between Lambda tasks.
 * Each step receives the previous step's output as its input, accumulating
 * state (inventoryDeducted, paymentTransactionId) so compensation functions
 * know exactly what to roll back.
 */
public class SagaContext {

    private final String orderId;
    private final String productId;
    private final int qty;
    private final String userId;
    private final double unitPrice;
    private final boolean inventoryDeducted;
    private final String paymentTransactionId;

    private SagaContext(Builder b) {
        this.orderId              = b.orderId;
        this.productId            = b.productId;
        this.qty                  = b.qty;
        this.userId               = b.userId;
        this.unitPrice            = b.unitPrice;
        this.inventoryDeducted    = b.inventoryDeducted;
        this.paymentTransactionId = b.paymentTransactionId;
    }

    public static SagaContext from(OrderRequest req) {
        return new Builder()
                .orderId(req.orderId())
                .productId(req.productId())
                .qty(req.qty())
                .userId(req.userId())
                .build();
    }

    public SagaContext withUnitPrice(double price) {
        return toBuilder().unitPrice(price).build();
    }

    public SagaContext withInventoryDeducted() {
        return toBuilder().inventoryDeducted(true).build();
    }

    public SagaContext withPaymentTransactionId(String txId) {
        return toBuilder().paymentTransactionId(txId).build();
    }

    // ── getters ──────────────────────────────────────────────────────────────

    public String getOrderId()              { return orderId; }
    public String getProductId()            { return productId; }
    public int    getQty()                  { return qty; }
    public String getUserId()               { return userId; }
    public double getUnitPrice()            { return unitPrice; }
    public boolean isInventoryDeducted()    { return inventoryDeducted; }
    public String getPaymentTransactionId() { return paymentTransactionId; }
    public double getTotalAmount()          { return unitPrice * qty; }

    private Builder toBuilder() {
        return new Builder()
                .orderId(orderId).productId(productId).qty(qty).userId(userId)
                .unitPrice(unitPrice).inventoryDeducted(inventoryDeducted)
                .paymentTransactionId(paymentTransactionId);
    }

    @Override
    public String toString() {
        return "SagaContext{orderId=" + orderId + ", productId=" + productId
                + ", qty=" + qty + ", inventoryDeducted=" + inventoryDeducted
                + ", paymentTxId=" + paymentTransactionId + "}";
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    static class Builder {
        private String  orderId;
        private String  productId;
        private int     qty;
        private String  userId;
        private double  unitPrice;
        private boolean inventoryDeducted;
        private String  paymentTransactionId;

        Builder orderId(String v)              { this.orderId = v;              return this; }
        Builder productId(String v)            { this.productId = v;            return this; }
        Builder qty(int v)                     { this.qty = v;                  return this; }
        Builder userId(String v)               { this.userId = v;               return this; }
        Builder unitPrice(double v)            { this.unitPrice = v;            return this; }
        Builder inventoryDeducted(boolean v)   { this.inventoryDeducted = v;    return this; }
        Builder paymentTransactionId(String v) { this.paymentTransactionId = v; return this; }
        SagaContext build()                    { return new SagaContext(this); }
    }
}
