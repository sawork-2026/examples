package com.example.pipeline;

import com.example.pipeline.model.EnrichedOrder;
import com.example.pipeline.model.OrderRequest;
import com.example.pipeline.model.PricedOrder;
import com.example.pipeline.service.ProductCatalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Defines a 4-stage order processing pipeline using Spring Cloud Function composition.
 *
 * Each stage is an independent Function bean — independently testable, independently
 * deployable as a Lambda, and composable in any order.
 *
 * Two composition styles are shown:
 *
 * 1. Programmatic (orderPipeline bean): explicit Java andThen() chaining.
 *    Best for type safety and IDE support.
 *
 * 2. YAML (application.yml definition key): Spring Cloud Function routes
 *    POST /orderPipeline to the composed function automatically.
 *    Best for reconfiguring pipelines without code changes.
 *
 * Pipeline stages:
 *   validateOrder       — guard clause: reject bad input early
 *   enrichWithProduct   — fetch product name/price from catalog (BaaS)
 *   calculateTotal      — apply bulk discount (≥5 items → 10% off)
 *   applyDiscount       — apply coupon code on top of bulk discount
 */
@Configuration
public class FunctionConfig {

    /** Stage 1: Validate input. Throws early so downstream stages never see bad data. */
    @Bean
    public Function<OrderRequest, OrderRequest> validateOrder() {
        return request -> {
            if (request.productId() == null || request.productId().isBlank()) {
                throw new IllegalArgumentException("productId is required");
            }
            if (request.qty() <= 0) {
                throw new IllegalArgumentException("qty must be positive, got: " + request.qty());
            }
            if (request.userId() == null || request.userId().isBlank()) {
                throw new IllegalArgumentException("userId is required");
            }
            return request;
        };
    }

    /** Stage 2: Enrich with product details from the catalog. */
    @Bean
    public Function<OrderRequest, EnrichedOrder> enrichWithProduct(ProductCatalog catalog) {
        return request -> {
            if (!catalog.exists(request.productId())) {
                throw new IllegalArgumentException("Product not found: " + request.productId());
            }
            return EnrichedOrder.from(request,
                    catalog.nameOf(request.productId()),
                    catalog.priceOf(request.productId()),
                    catalog.categoryOf(request.productId()));
        };
    }

    /**
     * Stage 3: Apply bulk discount.
     * Buying 5 or more of any item gives a 10% volume discount.
     */
    @Bean
    public Function<EnrichedOrder, EnrichedOrder> calculateTotal() {
        return order -> {
            if (order.qty() >= 5) {
                // Rebuild with bulk discount baked into the unit price
                double discountedPrice = order.unitPrice() * 0.90;
                return new EnrichedOrder(order.productId(), order.productName(), order.qty(),
                        order.userId(), order.couponCode(), discountedPrice, order.category());
            }
            return order;
        };
    }

    /**
     * Stage 4: Apply coupon code on top of any existing discount.
     * Produces the final PricedOrder ready for checkout.
     */
    @Bean
    public Function<EnrichedOrder, PricedOrder> applyDiscount(ProductCatalog catalog) {
        return order -> {
            double couponRate = catalog.couponDiscount(order.couponCode());
            return PricedOrder.from(order, couponRate);
        };
    }

    /**
     * Composed pipeline: programmatic composition via andThen().
     *
     * Equivalent to the YAML definition:
     *   spring.cloud.function.definition: validateOrder|enrichWithProduct|calculateTotal|applyDiscount
     *
     * Registered as a named bean so tests can look it up from FunctionCatalog by name.
     */
    @Bean
    public Function<OrderRequest, PricedOrder> orderPipeline(
            Function<OrderRequest, OrderRequest> validateOrder,
            Function<OrderRequest, EnrichedOrder> enrichWithProduct,
            Function<EnrichedOrder, EnrichedOrder> calculateTotal,
            Function<EnrichedOrder, PricedOrder> applyDiscount) {
        return validateOrder
                .andThen(enrichWithProduct)
                .andThen(calculateTotal)
                .andThen(applyDiscount);
    }
}
