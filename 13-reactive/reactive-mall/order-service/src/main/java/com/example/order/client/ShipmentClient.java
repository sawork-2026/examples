package com.example.order.client;

import com.example.order.model.ShipmentRequest;
import com.example.order.model.ShipmentResult;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 物流服务 WebClient 封装。
 */
@Component
public class ShipmentClient {

    private final WebClient client;

    public ShipmentClient(WebClient upstreamClient) {
        this.client = upstreamClient;
    }

    public Mono<ShipmentResult> createShipment(ShipmentRequest req) {
        return client.post()
                .uri("/api/shipments")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ShipmentResult.class);
    }
}
