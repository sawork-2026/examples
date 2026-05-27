package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Reactor 错误处理策略演示：timeout + retryWhen + onErrorResume 组合。
 *
 * 模拟一个不稳定的远程调用，通过 4 个场景展示错误处理管道如何工作：
 *   1) 首次成功 → 直接拿到结果
 *   2) 一直失败 → 重试耗尽 → 降级到缓存
 *   3) 前 2 次失败、第 3 次成功 → retryWhen 救回来
 *   4) 响应太慢触发 timeout → 也走缓存兜底
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E08_ErrorHandling
 */
public class E08_ErrorHandling {

    // 模拟远程调用：前 failuresBeforeSuccess 次抛异常，之后返回成功
    // Mono.defer() 保证每次订阅（含重试）都重新执行 lambda
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

    // 降级数据源：模拟从缓存拿数据
    static Mono<String> cacheFallback() {
        return Mono.just("cached-data").doOnNext(v -> System.out.println("    fallback hit: " + v));
    }

    // 组合管道：timeout → retryWhen → onErrorResume
    // 执行顺序：
    //   1. 单次调用超过 300ms → TimeoutException
    //   2. retryWhen 最多重试 2 次，指数退避（100ms → 200ms）
    //   3. 仅对 RuntimeException 重试，其余直接放行
    //   4. 重试全部耗尽或非 RuntimeException → onErrorResume 走缓存
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
        // 场景 1: 0 次失败，50ms 延迟 → 一次成功
        System.out.println("\n=== 1) 成功 ===");
        System.out.println("  result = " + protectedCall(0, 50).block());

        // 场景 2: 5 次失败（超过重试上限 2 次）→ 重试耗尽 → 走缓存
        System.out.println("\n=== 2) 一直失败 → 走缓存兜底 ===");
        System.out.println("  result = " + protectedCall(5, 50).block());

        // 场景 3: 前 2 次失败，第 3 次成功 → 刚好被 retryWhen 救回
        System.out.println("\n=== 3) 前 2 次失败、第 3 次成功 ===");
        System.out.println("  result = " + protectedCall(2, 50).block());

        // 场景 4: 0 次失败但延迟 500ms > timeout 300ms → TimeoutException → 走缓存
        System.out.println("\n=== 4) 慢调用触发 timeout → 也走缓存 ===");
        System.out.println("  result = " + protectedCall(0, 500).block());
    }
}
