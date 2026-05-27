package com.example.upstream.payment;

import com.example.upstream.chaos.ChaosMode;
import com.example.upstream.model.PaymentRequest;
import com.example.upstream.model.PaymentResult;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentStore store;
    private final ChaosMode chaos;

    public PaymentController(PaymentStore store, ChaosMode chaos) {
        this.store = store;
        this.chaos = chaos;
    }

    @PostMapping
    public Mono<PaymentResult> create(@RequestBody PaymentRequest req) {
        return chaos.apply(Mono.just(store.create(req.orderId(), req.amount())));
    }

    @GetMapping("/{paymentId}")
    public Mono<PaymentResult> query(@PathVariable String paymentId) {
        PaymentResult result = store.query(paymentId);
        if (result == null) {
            return Mono.error(new RuntimeException("Payment not found: " + paymentId));
        }
        // 查询端点不受 chaos 影响（否则轮询会因 chaos 干扰变得不可预测）
        return Mono.just(result);
    }
}
