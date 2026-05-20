package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 把 timeout / onErrorResume / retryWhen 组合在同一条链上，跑三个场景对比效果：
 *   1) 成功
 *   2) 一直失败 → 退化到缓存
 *   3) 前两次失败、第三次成功 → 通过 retryWhen 救回来
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.ErrorHandlingDemo
 */
public class ErrorHandlingDemo {

    static Mono<String> remoteCall(int failuresBeforeSuccess, AtomicInteger attempts, long latencyMs) {
        return Mono.defer(() -> {
            int n = attempts.incrementAndGet();
            System.out.println("    remoteCall attempt #" + n);
            if (n <= failuresBeforeSuccess) {
                return Mono.<String>error(new RuntimeException("transient failure #" + n))
                        .delaySubscription(Duration.ofMillis(latencyMs));
            }
            return Mono.just("fresh-data-" + n)
                    .delayElement(Duration.ofMillis(latencyMs));
        });
    }

    static Mono<String> cacheFallback() {
        return Mono.just("cached-data").doOnNext(v -> System.out.println("    fallback hit: " + v));
    }

    static Mono<String> protectedCall(int failures, long latencyMs) {
        AtomicInteger attempts = new AtomicInteger();
        return remoteCall(failures, attempts, latencyMs)
                .timeout(Duration.ofMillis(300))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100))
                        .filter(e -> e instanceof RuntimeException))
                .onErrorResume(e -> {
                    System.out.println("    retries exhausted: " + e);
                    return cacheFallback();
                });
    }

    public static void main(String[] args) {
        System.out.println("\n=== 1) 成功 ===");
        System.out.println("  result = " + protectedCall(0, 50).block());

        System.out.println("\n=== 2) 一直失败 → 走缓存兜底 ===");
        System.out.println("  result = " + protectedCall(5, 50).block());

        System.out.println("\n=== 3) 前 2 次失败、第 3 次成功 ===");
        System.out.println("  result = " + protectedCall(2, 50).block());

        System.out.println("\n=== 4) 慢调用触发 timeout → 也走缓存 ===");
        System.out.println("  result = " + protectedCall(0, 500).block());
    }
}
