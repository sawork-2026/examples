package com.example.reactor;

import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * publishOn vs subscribeOn 线程切换演示。
 *
 * Reactor 默认在当前线程执行全部算子（不自动分配线程），需要手动指定 Scheduler：
 *   - subscribeOn(scheduler): 切换源头（订阅时）的执行线程，只有最靠近源的一次生效
 *   - publishOn(scheduler):  切换下游操作符的执行线程，每次调用都切一段
 *
 * 常用 Scheduler：
 *   Schedulers.parallel()       — CPU 密集型，线程数 = CPU cores
 *   Schedulers.boundedElastic() — 包装阻塞 I/O，弹性线程池
 *   Schedulers.single()         — 单线程，低延迟任务
 *   Schedulers.immediate()      — 当前线程（默认）
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E07_SchedulerThreads
 */
public class E07_SchedulerThreads {

    static void log(String stage, Object value) {
        System.out.printf("  [%s] %s -> %s%n",
                Thread.currentThread().getName(), stage, value);
    }

    public static void main(String[] args) throws InterruptedException {

        // 场景 1: 不加 Scheduler → 全部在 main 线程执行
        System.out.println("\n=== 1) 不加任何 Scheduler：全在 main ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(50);

        // 场景 2: subscribeOn → 源头和所有上下游都跑在指定的线程上
        // 适合把整条阻塞链路搬到 boundedElastic 上
        System.out.println("\n=== 2) subscribeOn(boundedElastic)：影响源头与上游 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(100);

        // 场景 3: publishOn → 只影响它之后的操作符
        // map1 仍在 main 上跑，map2 和 subscribe 切到 parallel
        System.out.println("\n=== 3) publishOn(parallel)：切换下游线程 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .publishOn(Schedulers.parallel())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(100);

        // 场景 4: 同时使用 subscribeOn + publishOn
        // subscribeOn 决定源头 → map1 在 boundedElastic 上
        // publishOn 从中间切换 → map2 和 subscribe 在 parallel 上
        System.out.println("\n=== 4) 同时使用：subscribeOn 决定起点，publishOn 切换中段 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(200);

        // 场景 5: 多个 subscribeOn 只有最靠近源头的那个生效
        // parallel 比 boundedElastic 更靠近 Flux.range → parallel 胜出
        System.out.println("\n=== 5) 多个 subscribeOn 只有最靠近源头的生效 ===");
        Flux.range(1, 3)
                .subscribeOn(Schedulers.parallel())          // ← 这个更靠近源，胜出
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())    // ← 被忽略
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(200);
    }
}
