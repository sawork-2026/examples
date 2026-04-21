package com.example.pipeline;

import com.example.pipeline.model.EnrichedOrder;
import com.example.pipeline.model.OrderRequest;
import com.example.pipeline.model.PricedOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class FunctionPipelineTests {

    @Autowired
    private FunctionCatalog catalog;

    // ── Full pipeline ─────────────────────────────────────────────────────────

    @Test
    void pipeline_basicOrder_correctTotal() {
        PricedOrder result = pipeline().apply(
                new OrderRequest("CAT-001", 2, "user-1", null));

        assertThat(result.productName()).isEqualTo("Persian Cat");
        assertThat(result.unitPrice()).isEqualTo(120.00);
        assertThat(result.subtotal()).isEqualTo(240.00);
        assertThat(result.discountRate()).isEqualTo(0.0);   // no coupon, qty < 5
        assertThat(result.finalPrice()).isEqualTo(240.00);
    }

    @Test
    void pipeline_bulkOrder_tenPercentDiscount() {
        // qty >= 5 → 10% bulk discount applied in calculateTotal
        PricedOrder result = pipeline().apply(
                new OrderRequest("FISH-001", 10, "user-2", null));

        assertThat(result.unitPrice()).isEqualTo(15.00 * 0.90); // bulk price
        assertThat(result.finalPrice()).isEqualTo(15.00 * 0.90 * 10);
    }

    @Test
    void pipeline_withCoupon_couponDiscountOnTop() {
        // qty < 5 (no bulk), but SAVE10 coupon → 10% off subtotal
        PricedOrder result = pipeline().apply(
                new OrderRequest("DOG-001", 1, "user-3", "SAVE10"));

        assertThat(result.discountRate()).isEqualTo(0.10);
        assertThat(result.finalPrice()).isEqualTo(150.00 * 0.90);
    }

    @Test
    void pipeline_bulkPlusCoupon_bothDiscountsApplied() {
        // qty >= 5 → unit price already discounted 10%, then coupon SAVE20 on subtotal
        PricedOrder result = pipeline().apply(
                new OrderRequest("CAT-001", 5, "user-4", "SAVE20"));

        double bulkPrice = 120.00 * 0.90;           // 108.00 per unit after bulk discount
        double subtotal  = bulkPrice * 5;            // 540.00
        double expected  = subtotal * (1 - 0.20);    // 432.00 after SAVE20

        assertThat(result.finalPrice()).isEqualTo(expected);
    }

    // ── Validation stage ──────────────────────────────────────────────────────

    @Test
    void validateOrder_missingProductId_throws() {
        assertThatThrownBy(() -> pipeline().apply(new OrderRequest("", 1, "user-5", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId is required");
    }

    @Test
    void validateOrder_zeroQty_throws() {
        assertThatThrownBy(() -> pipeline().apply(new OrderRequest("DOG-001", 0, "user-6", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("qty must be positive");
    }

    // ── Enrichment stage ──────────────────────────────────────────────────────

    @Test
    void enrichWithProduct_unknownProduct_throws() {
        assertThatThrownBy(() -> pipeline().apply(new OrderRequest("UNKNOWN-999", 1, "user-7", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    // ── Individual stages testable in isolation ───────────────────────────────

    @Test
    void eachStageCanBeTestedInIsolation() {
        // validateOrder alone
        Function<OrderRequest, OrderRequest> validate = catalog.lookup("validateOrder");
        OrderRequest req = new OrderRequest("DOG-001", 2, "user-8", null);
        assertThat(validate.apply(req)).isEqualTo(req);

        // enrichWithProduct alone
        Function<OrderRequest, EnrichedOrder> enrich = catalog.lookup("enrichWithProduct");
        EnrichedOrder enriched = enrich.apply(req);
        assertThat(enriched.productName()).isEqualTo("Dalmatian");
        assertThat(enriched.unitPrice()).isEqualTo(150.00);
    }

    @SuppressWarnings("unchecked")
    private Function<OrderRequest, PricedOrder> pipeline() {
        return catalog.lookup("orderPipeline");
    }
}
