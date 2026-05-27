package com.example.order.client;

import com.example.order.model.DeductRequest;
import com.example.order.model.InventoryInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 库存服务 WebClient 封装。
 * 每个方法返回 Mono——调用是非阻塞的，只有 subscribe 时才真正发出 HTTP 请求。
 */
@Component
public class InventoryClient {

    private final WebClient client;

    public InventoryClient(WebClient upstreamClient) {
        this.client = upstreamClient;
    }

    public Mono<InventoryInfo> checkStock(String productId) {
        return client.get()
                .uri("/api/inventory/{id}", productId)
                .retrieve()
                .bodyToMono(InventoryInfo.class);
    }

    public Mono<Void> deductStock(DeductRequest req) {
        return client.post()
                .uri("/api/inventory/deduct")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
