package com.example.pipeline.model;

/** OrderRequest enriched with product details fetched from the catalog (BaaS). */
public record EnrichedOrder(
        String productId,
        String productName,
        int qty,
        String userId,
        String couponCode,
        double unitPrice,
        String category
) {
    public static EnrichedOrder from(OrderRequest req, String name, double price, String category) {
        return new EnrichedOrder(req.productId(), name, req.qty(), req.userId(),
                req.couponCode(), price, category);
    }
}
