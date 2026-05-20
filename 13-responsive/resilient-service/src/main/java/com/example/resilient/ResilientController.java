package com.example.resilient;

import java.time.Duration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 把同一个不稳定上游用三种方式调用，对比"无保护 / 有保护 / 加断路器"：
 *
 *   GET /raw          → 裸调用，错误直接 500
 *   GET /protected    → timeout + retry + onErrorResume 兜底
 *   GET /breaker      → 在 protected 之上再加 CircuitBreaker
 *   GET /mode/{m}     → 切换上游模式：OK / SLOW / FAIL / FLAKY
 *
 * 演示步骤：
 *   1) curl /raw      → 改 mode=FAIL → 再 curl /raw      （直接报错）
 *   2) curl /protected → 改 mode=FAIL → 再 curl /protected（拿到缓存兜底）
 *   3) 连续打 mode=FAIL 时的 /breaker，观察熔断打开后直接快速失败
 */
@RestController
public class ResilientController {

    private final FlakyUpstream upstream;
    private final CircuitBreaker breaker;

    public ResilientController(FlakyUpstream upstream) {
        this.upstream = upstream;
        this.breaker = CircuitBreaker.of("upstream", CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50f)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build());
    }

    @GetMapping("/raw")
    public Mono<String> raw() {
        return upstream.call();
    }

    @GetMapping("/protected")
    public Mono<String> guarded() {
        return upstream.call()
                .timeout(Duration.ofMillis(300))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                        .filter(e -> e instanceof RuntimeException))
                .onErrorResume(e -> Mono.just("fallback(" + e.getClass().getSimpleName() + ")"));
    }

    @GetMapping("/breaker")
    public Mono<String> breaker() {
        return upstream.call()
                .timeout(Duration.ofMillis(300))
                .transformDeferred(CircuitBreakerOperator.of(breaker))
                .onErrorResume(e -> Mono.just("fallback(" + e.getClass().getSimpleName()
                        + ", cb=" + breaker.getState() + ")"));
    }

    @GetMapping("/mode/{m}")
    public String mode(@PathVariable String m) {
        upstream.setMode(FlakyUpstream.Mode.valueOf(m.toUpperCase()));
        return "mode=" + upstream.getMode() + ", cb=" + breaker.getState();
    }
}
