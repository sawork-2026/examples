package com.example.serverless;

import com.example.serverless.model.OrderResult;
import com.example.serverless.model.Product;
import com.example.serverless.model.PurchaseRequest;
import com.example.serverless.model.SearchRequest;
import com.example.serverless.service.OrderService;
import com.example.serverless.service.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

/**
 * Defines the two FaaS functions that replace the JPetStore monolith's server-side logic.
 *
 * Spring Cloud Function routes HTTP POST requests by function name:
 *   POST /searchProducts  →  searchProducts bean
 *   POST /purchase        →  purchase bean
 *
 * The same beans are invoked by AWS Lambda via FunctionInvoker when deployed to the cloud.
 * No code change is needed between local HTTP mode and Lambda — only the adapter changes.
 *
 * Compare with the original monolith:
 *   CatalogController.searchProducts()  →  always-on servlet, tied to the server lifecycle
 *   OrderController.purchase()          →  same process, same JVM, same scaling unit
 *
 * After Serverless transformation:
 *   searchProducts  →  independent Lambda, scales to zero between requests
 *   purchase        →  independent Lambda, can be scaled separately from search
 */
@Configuration
public class FunctionConfig {

    /**
     * Searches the product catalog (BaaS in production: Firebase / DynamoDB).
     * Stateless: no instance variable, safe for concurrent Lambda execution.
     */
    @Bean
    public Function<SearchRequest, List<Product>> searchProducts(ProductRepository repo) {
        return request -> repo.search(request.keyword(), request.category());
    }

    /**
     * Processes a purchase order with idempotency guarantee.
     * The caller must supply a unique idempotencyKey; retries with the same key
     * return the original result without re-processing (safe under at-least-once delivery).
     */
    @Bean
    public Function<PurchaseRequest, OrderResult> purchase(OrderService orderService) {
        return orderService::processOrder;
    }
}
