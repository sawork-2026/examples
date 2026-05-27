package com.example.reactor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Flux / Mono 的各种创建方式，对应 slides 中 "Flux：0..N" 和 "Mono：0 或 1" 两页。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E01_FluxMonoCreation
 */
public class E01_FluxMonoCreation {

    public static void main(String[] args) throws InterruptedException {

        // =====================================================================
        // Flux<T>: 表示 0..N 个元素的异步序列，是 Publisher<T> 的实现
        // =====================================================================
        System.out.println("========== Flux 创建 ==========");

        // just(): 直接列举元素，最简单的创建方式
        System.out.println("\n--- Flux.just ---");
        Flux.just(1, 2, 3).subscribe(v -> System.out.println("  " + v));

        // range(start, count): 生成连续整数序列 [start, start+count)
        System.out.println("\n--- Flux.range(1, 5) ---");
        Flux.range(1, 5).subscribe(v -> System.out.println("  " + v));

        // fromIterable(): 从已有集合创建，适合桥接命令式代码
        System.out.println("\n--- Flux.fromIterable ---");
        Flux.fromIterable(List.of("alpha", "beta", "gamma"))
                .subscribe(v -> System.out.println("  " + v));

        // empty(): 0 个元素，直接发 onComplete
        System.out.println("\n--- Flux.empty ---");
        Flux.empty()
                .subscribe(
                        v -> System.out.println("  " + v),
                        e -> {},
                        () -> System.out.println("  onComplete (no elements)"));

        // error(): 直接发 onError，用于测试或表示已知的错误状态
        System.out.println("\n--- Flux.error ---");
        Flux.error(new RuntimeException("boom"))
                .subscribe(
                        v -> {},
                        e -> System.out.println("  onError: " + e.getMessage()));

        // generate(): 有状态的同步生成器，每次回调只能 next() 一个元素
        // 参数1: 初始状态工厂 () -> 0
        // 参数2: (当前状态, sink) -> 下一个状态
        System.out.println("\n--- Flux.generate (stateful, emit 5 then complete) ---");
        Flux.<Integer, Integer>generate(
                () -> 0,
                (state, sink) -> {
                    sink.next(state);
                    if (state == 4) sink.complete();
                    return state + 1;
                })
                .subscribe(v -> System.out.println("  " + v));

        // interval(): 按固定间隔发射递增的 Long 值（0, 1, 2, ...）
        // 注意：interval 在 parallel Scheduler 上异步执行，所以需要 latch 等待
        System.out.println("\n--- Flux.interval (every 200ms, take 5) ---");
        CountDownLatch latch = new CountDownLatch(1);
        Flux.interval(Duration.ofMillis(200))
                .take(5)
                .subscribe(
                        v -> System.out.println("  tick " + v),
                        e -> {},
                        () -> { System.out.println("  done"); latch.countDown(); });
        latch.await(3, TimeUnit.SECONDS);

        // =====================================================================
        // Mono<T>: 表示 0 或 1 个元素的异步结果，≈ CompletableFuture<T>
        // =====================================================================
        System.out.println("\n========== Mono 创建 ==========");

        // just(): 已知确定值
        System.out.println("\n--- Mono.just ---");
        Mono.just(42).subscribe(v -> System.out.println("  " + v));

        // empty(): 无值，仅发 onComplete；典型场景：findById 没找到
        System.out.println("\n--- Mono.empty ---");
        Mono.empty()
                .subscribe(
                        v -> System.out.println("  " + v),
                        e -> {},
                        () -> System.out.println("  onComplete (empty)"));

        // fromCallable(): 把阻塞调用包装成 Mono，配合 subscribeOn 可以不阻塞调用线程
        System.out.println("\n--- Mono.fromCallable (wrapping blocking call) ---");
        Mono.fromCallable(() -> {
            Thread.sleep(50);
            return "result from blocking I/O";
        }).subscribe(v -> System.out.println("  " + v));

        // defer(): 延迟创建——每次 subscribe 时才执行 lambda 产生新的 Mono
        // 对比 just(): just 在定义时就捕获值，defer 在订阅时才求值
        System.out.println("\n--- Mono.defer (lazy, evaluated at subscribe time) ---");
        Mono<Long> deferred = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        deferred.subscribe(v -> System.out.println("  1st subscribe: " + v));
        Thread.sleep(100);
        deferred.subscribe(v -> System.out.println("  2nd subscribe: " + v + " (different!)"));

        // delay(): 延迟指定时间后发射一个 0L，类似 Timer
        System.out.println("\n--- Mono.delay (100ms) ---");
        CountDownLatch latch2 = new CountDownLatch(1);
        Mono.delay(Duration.ofMillis(100))
                .subscribe(
                        v -> System.out.println("  delayed value: " + v),
                        e -> {},
                        latch2::countDown);
        latch2.await(1, TimeUnit.SECONDS);
    }
}
