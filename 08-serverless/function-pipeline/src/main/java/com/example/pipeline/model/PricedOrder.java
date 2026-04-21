package com.example.pipeline.model;

/** Final output of the pipeline: fully priced and discounted order ready for checkout. */
public record PricedOrder(
        String productId,
        String productName,
        int qty,
        String userId,
        double unitPrice,
        double subtotal,
        double discountRate,
        double discountAmount,
        double finalPrice
) {
    public static PricedOrder from(EnrichedOrder order, double discountRate) {
        double subtotal = order.unitPrice() * order.qty();
        double discountAmount = subtotal * discountRate;
        return new PricedOrder(
                order.productId(), order.productName(), order.qty(), order.userId(),
                order.unitPrice(), subtotal, discountRate, discountAmount,
                subtotal - discountAmount
        );
    }
}
