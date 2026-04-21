package com.example.serverless;

import com.example.serverless.model.OrderResult;
import com.example.serverless.model.Product;
import com.example.serverless.model.PurchaseRequest;
import com.example.serverless.model.SearchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.function.context.FunctionCatalog;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests functions directly via FunctionCatalog — no HTTP server needed.
 * This is the recommended approach for unit/integration testing FaaS functions:
 * the same code path is exercised whether the function runs locally or on Lambda.
 */
@SpringBootTest
class JpetstoreServerlessApplicationTests {

    @Autowired
    private FunctionCatalog catalog;

    // ── searchProducts ───────────────────────────────────────────────────────

    @Test
    void searchByKeyword_returnsMatchingProducts() {
        List<Product> results = search(new SearchRequest("golden", null));
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(p -> p.name().toLowerCase().contains("golden"));
    }

    @Test
    void searchByCategory_returnsOnlyThatCategory() {
        List<Product> results = search(new SearchRequest(null, "Fish"));
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(p -> p.category().equals("Fish"));
    }

    @Test
    void searchWithNoFilter_returnsAllProducts() {
        List<Product> results = search(new SearchRequest(null, null));
        assertThat(results).hasSizeGreaterThan(5);
    }

    @Test
    void searchWithNonExistentKeyword_returnsEmptyList() {
        List<Product> results = search(new SearchRequest("zzznomatch", null));
        assertThat(results).isEmpty();
    }

    // ── purchase ─────────────────────────────────────────────────────────────

    @Test
    void purchase_validRequest_returnsSuccess() {
        OrderResult result = purchase(new PurchaseRequest("DOG-001", 1, "user-001", "key-01"));
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.orderId()).isNotBlank();
    }

    @Test
    void purchase_isIdempotent() {
        // Same idempotency key → same orderId, regardless of retry count.
        // This is the core safety guarantee needed under Lambda's at-least-once delivery.
        PurchaseRequest request = new PurchaseRequest("CAT-001", 2, "user-002", "key-idem-01");
        OrderResult first  = purchase(request);
        OrderResult second = purchase(request);
        assertThat(first.orderId()).isEqualTo(second.orderId());
        assertThat(first.status()).isEqualTo("SUCCESS");
    }

    @Test
    void purchase_unknownProduct_returnsRejected() {
        OrderResult result = purchase(new PurchaseRequest("UNKNOWN-999", 1, "user-003", "key-02"));
        assertThat(result.status()).isEqualTo("REJECTED");
    }

    @Test
    void purchase_invalidQty_returnsRejected() {
        OrderResult result = purchase(new PurchaseRequest("DOG-001", 0, "user-004", "key-03"));
        assertThat(result.status()).isEqualTo("REJECTED");
    }

    @Test
    void purchase_missingIdempotencyKey_returnsRejected() {
        OrderResult result = purchase(new PurchaseRequest("DOG-001", 1, "user-005", ""));
        assertThat(result.status()).isEqualTo("REJECTED");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<Product> search(SearchRequest request) {
        Function<SearchRequest, List<Product>> fn = catalog.lookup("searchProducts");
        return fn.apply(request);
    }

    private OrderResult purchase(PurchaseRequest request) {
        Function<PurchaseRequest, OrderResult> fn = catalog.lookup("purchase");
        return fn.apply(request);
    }
}
