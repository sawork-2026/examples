package com.example.order.client;

import com.example.order.model.PaymentRequest;
import com.example.order.model.PaymentResult;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * 支付服务 WebClient 封装。
 * createPayment 返回 PENDING 状态的支付单，queryPayment 用于轮询直到 PAID。
 */
@Component
public class PaymentClient {

    private final WebClient client;

    public PaymentClient(WebClient upstreamClient) {
        this.client = upstreamClient;
    }

    public Mono<PaymentResult> createPayment(PaymentRequest req) {
        return client.post()
                .uri("/api/payments")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(PaymentResult.class);
    }

    public Mono<PaymentResult> queryPayment(String paymentId) {
        return client.get()
                .uri("/api/payments/{id}", paymentId)
                .retrieve()
                .bodyToMono(PaymentResult.class);
    }
}
