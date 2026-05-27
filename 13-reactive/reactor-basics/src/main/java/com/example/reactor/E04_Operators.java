package com.example.reactor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

/**
 * 演示 Reactor 常用操作符，对应 slides "Operators · 元素变换" / "聚合 / 截取 / 组合"
 * / "Operating on a Stream" 三页。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E04_Operators
 */
public class E04_Operators {

    public static void main(String[] args) throws InterruptedException {

        // ======================== 元素变换 ========================

        // filter(predicate): 按条件筛选，不满足的元素被丢弃
        // 弹珠图：--1--2--3--4--5--| → filter(偶数) → --2--4--|
        System.out.println("=== 1) filter: 只留偶数 ===");
        Flux.range(1, 10)
                .filter(i -> i % 2 == 0)
                .subscribe(v -> System.out.println("  " + v));

        // map(fn): 一对一同步变换每个元素
        // 弹珠图：--"hello"--"reactor"--| → map(toUpperCase) → --"HELLO"--"REACTOR"--|
        System.out.println("\n=== 2) map: 一对一变换 ===");
        Flux.just("hello", "reactor", "world")
                .map(String::toUpperCase)
                .subscribe(v -> System.out.println("  " + v));

        // flatMap(fn): 一对多异步展开——每个元素变成一个 Publisher，再把结果打平
        // 常用于"拿到 id 列表后并发查详情"的场景
        // 注意：flatMap 不保证顺序，需要保序用 concatMap
        System.out.println("\n=== 3) flatMap: 一对多展开 (每个 id 查详情) ===");
        Flux.just(1, 2, 3)
                .flatMap(id -> Flux.just("detail-" + id + "-a", "detail-" + id + "-b"))
                .subscribe(v -> System.out.println("  " + v));

        // index(): 给每个元素打上从 0 开始的序号，包装成 Tuple2<Long, T>
        System.out.println("\n=== 4) index: 给元素打序号 → Tuple2<Long, T> ===");
        Flux.just("alpha", "beta", "gamma")
                .index()
                .subscribe(t -> System.out.println("  [" + t.getT1() + "] " + t.getT2()));

        // ======================== 截取 ========================

        // take(n): 只取前 n 个元素，然后发 onComplete 并 cancel 上游
        // 对无限流特别有用（如 Flux.interval）
        System.out.println("\n=== 5) take: 只取前 3 个 ===");
        Flux.range(1, 100)
                .take(3)
                .subscribe(v -> System.out.println("  " + v));

        // ======================== 聚合 ========================

        // buffer(n): 每 n 个元素打包成一个 List<T> 发出
        // 8 个元素 buffer(3) → [1,2,3], [4,5,6], [7,8]（最后一组不足 3 个也发出）
        System.out.println("\n=== 6) buffer(3): 每 3 个打包成 List ===");
        Flux.range(1, 8)
                .buffer(3)
                .subscribe(list -> System.out.println("  batch: " + list));

        // window(n): 类似 buffer，但切出来的是子 Flux<T> 而不是 List
        // Flux<T> → Flux<Flux<T>>，适合流式处理每个窗口
        System.out.println("\n=== 7) window(3): 每 3 个切成子 Flux ===");
        CountDownLatch latch = new CountDownLatch(1);
        Flux.range(1, 8)
                .window(3)
                .index()
                .subscribe(
                        windowTuple -> {
                            long idx = windowTuple.getT1();
                            windowTuple.getT2()
                                    .collectList()
                                    .subscribe(list -> System.out.println("  window[" + idx + "]: " + list));
                        },
                        e -> {},
                        latch::countDown);
        latch.await(1, TimeUnit.SECONDS);

        // ======================== 组合 ========================

        // zipWith(other, combinator): 把两条 Flux 按位置一一配对
        // 当其中一条结束时，整个 zip 就结束（较短的那条决定长度）
        System.out.println("\n=== 8) zipWith: 两条流按位配对 ===");
        Flux.just(1, 2, 3, 4)
                .map(i -> i * 2)                                    // [2, 4, 6, 8]
                .zipWith(Flux.range(0, Integer.MAX_VALUE),          // [0, 1, 2, ...]
                        (a, b) -> "First: " + a + ", Second: " + b)
                .subscribe(v -> System.out.println("  " + v));

        // ======================== 组合链 ========================

        // 把多个操作符串起来形成管道——这才是 Reactor 的实际使用方式
        // .log() 让你看到 zipWith 如何交替从两条流拉取，以及 cancel 信号
        System.out.println("\n=== 9) 组合链: slides 里的完整示例 ===");
        System.out.println("Flux.just(1,2,3,4).map(i*2).zipWith(Flux.range(0,MAX)):\n");
        Flux.just(1, 2, 3, 4)
                .log()
                .map(i -> i * 2)
                .zipWith(Flux.range(0, Integer.MAX_VALUE),
                        (a, b) -> "First: " + a + ", Second: " + b)
                .subscribe(v -> System.out.println("  → " + v));

        // ======================== 归约 ========================

        // reduce(): 将所有元素归约为一个值，返回 Mono<T>
        System.out.println("\n=== 10) reduce / collectList ===");
        Flux.range(1, 5)
                .reduce(Integer::sum)
                .subscribe(v -> System.out.println("  sum(1..5) = " + v));

        // collectList(): 将 Flux<T> 收集为 Mono<List<T>>
        // Flux → Mono 的转换之一
        Flux.just("a", "b", "c")
                .collectList()
                .subscribe(v -> System.out.println("  collectList = " + v));

        // ======================== 去重 ========================

        // distinct(): 全局去重（维护一个 HashSet）
        // distinctUntilChanged(): 只去除相邻重复（不维护全局状态，更省内存）
        System.out.println("\n=== 11) distinct / distinctUntilChanged ===");
        Flux.just(1, 1, 2, 3, 3, 2, 1)
                .distinct()
                .collectList()
                .subscribe(v -> System.out.println("  distinct: " + v));         // [1, 2, 3]

        Flux.just(1, 1, 2, 3, 3, 2, 1)
                .distinctUntilChanged()
                .collectList()
                .subscribe(v -> System.out.println("  distinctUntilChanged: " + v)); // [1, 2, 3, 2, 1]
    }
}
