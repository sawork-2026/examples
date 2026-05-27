package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 进阶操作符演示。E04 覆盖了基础变换/截取/聚合，这里展示更贴近生产的高级用法。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E04b_AdvancedOperators
 */
public class E04b_AdvancedOperators {

    public static void main(String[] args) throws InterruptedException {

        // =================================================================
        // 1) flatMap vs concatMap：并发 vs 保序
        //    flatMap: 所有内部 Publisher 并发订阅，结果交错到达，不保证顺序
        //    concatMap: 等上一个内部 Publisher 完成后才订阅下一个，严格保序
        //    选型：需要顺序用 concatMap，需要吞吐用 flatMap
        // =================================================================
        System.out.println("=== 1) flatMap vs concatMap ===");

        // flatMap: 3 个内部流并发执行，结果可能乱序（delay 造成 3 先于 1 到达）
        System.out.println("\n--- flatMap (并发，可能乱序) ---");
        CountDownLatch latch1 = new CountDownLatch(1);
        Flux.just(3, 1, 2)
                .flatMap(i -> Mono.just("item-" + i)
                        .delayElement(Duration.ofMillis(i * 50)))   // 3 延 150ms, 1 延 50ms, 2 延 100ms
                .subscribe(
                        v -> System.out.println("  " + v),
                        e -> {},
                        latch1::countDown);
        latch1.await(1, TimeUnit.SECONDS);

        // concatMap: 严格按 3 → 1 → 2 的输入顺序输出
        System.out.println("\n--- concatMap (串行，严格保序) ---");
        CountDownLatch latch2 = new CountDownLatch(1);
        Flux.just(3, 1, 2)
                .concatMap(i -> Mono.just("item-" + i)
                        .delayElement(Duration.ofMillis(i * 50)))
                .subscribe(
                        v -> System.out.println("  " + v),
                        e -> {},
                        latch2::countDown);
        latch2.await(1, TimeUnit.SECONDS);

        // =================================================================
        // 2) switchMap：新元素到达时取消上一个内部 Publisher
        //    典型场景：搜索框输入联想——用户每敲一个字就触发新查询，旧查询应被取消
        // =================================================================
        System.out.println("\n=== 2) switchMap：新元素取消旧的 ===");
        CountDownLatch latch3 = new CountDownLatch(1);
        Flux.just("re", "rea", "reac", "react")
                .delayElements(Duration.ofMillis(80))                 // 模拟用户每 80ms 敲一个字
                .switchMap(query ->
                        Mono.just("搜索结果: " + query)
                                .delayElement(Duration.ofMillis(150))) // 模拟搜索 API 要 150ms
                .subscribe(
                        v -> System.out.println("  " + v),            // 只有最后一个 "react" 的结果能到达
                        e -> {},
                        latch3::countDown);
        latch3.await(2, TimeUnit.SECONDS);
        System.out.println("  (前面的 re/rea/reac 查询被取消了，只有 react 的结果到达)");

        // =================================================================
        // 3) merge vs concat：多源合并策略
        //    merge: 同时订阅所有源，谁先产出谁先到（交错）
        //    concat: 按顺序拼接，前一个完成后才订阅下一个
        // =================================================================
        System.out.println("\n=== 3) merge vs concat ===");
        Flux<String> fast = Flux.just("F1", "F2").delayElements(Duration.ofMillis(50));
        Flux<String> slow = Flux.just("S1", "S2").delayElements(Duration.ofMillis(120));

        System.out.println("\n--- merge (交错到达) ---");
        CountDownLatch latch4 = new CountDownLatch(1);
        Flux.merge(fast, slow)
                .elapsed()          // 附上距上一个元素的时间间隔 (ms)
                .subscribe(
                        t -> System.out.println("  +" + t.getT1() + "ms " + t.getT2()),
                        e -> {},
                        latch4::countDown);
        latch4.await(1, TimeUnit.SECONDS);

        // 重新创建（cold publisher 已消费完）
        fast = Flux.just("F1", "F2").delayElements(Duration.ofMillis(50));
        slow = Flux.just("S1", "S2").delayElements(Duration.ofMillis(120));

        System.out.println("\n--- concat (先 fast 全部完成，再 slow) ---");
        CountDownLatch latch5 = new CountDownLatch(1);
        Flux.concat(fast, slow)
                .elapsed()
                .subscribe(
                        t -> System.out.println("  +" + t.getT1() + "ms " + t.getT2()),
                        e -> {},
                        latch5::countDown);
        latch5.await(1, TimeUnit.SECONDS);

        // =================================================================
        // 4) groupBy：按 key 分组，返回 Flux<GroupedFlux<K, V>>
        //    每个 GroupedFlux 是一个独立的子流，可以分别处理
        //    场景：日志按级别分流、订单按状态分组统计
        // =================================================================
        System.out.println("\n=== 4) groupBy: 按奇偶分组 ===");
        Flux.range(1, 8)
                .groupBy(i -> i % 2 == 0 ? "偶数" : "奇数")
                .flatMap(group ->
                        group.collectList()
                                .map(list -> group.key() + ": " + list))
                .subscribe(v -> System.out.println("  " + v));

        // =================================================================
        // 5) scan：运行时累加器，与 reduce 不同的是它发射每一步的中间结果
        //    reduce: [1,2,3,4,5] → Mono(15)            只有最终结果
        //    scan:   [1,2,3,4,5] → [1, 3, 6, 10, 15]   每步都发射
        //    场景：实时计算累计金额、滑动统计
        // =================================================================
        System.out.println("\n=== 5) scan: 运行累加，发射中间结果 ===");
        Flux.range(1, 5)
                .scan(0, Integer::sum)   // seed=0, (acc, next) -> acc + next
                .subscribe(v -> System.out.println("  running sum: " + v));

        // =================================================================
        // 6) switchIfEmpty：源为空时切换到备用 Publisher
        //    slides 中的 Reactive 版业务逻辑就用了这个：
        //    getFavorites().flatMap(getDetails).switchIfEmpty(getSuggestions)
        // =================================================================
        System.out.println("\n=== 6) switchIfEmpty: 空则走备用源 ===");

        // 有数据 → switchIfEmpty 不触发
        Flux.just("data-1", "data-2")
                .switchIfEmpty(Flux.just("fallback-1"))
                .subscribe(v -> System.out.println("  有数据: " + v));

        // 空流 → switchIfEmpty 生效
        Flux.<String>empty()
                .switchIfEmpty(Flux.just("fallback-1", "fallback-2"))
                .subscribe(v -> System.out.println("  空流降级: " + v));

        // =================================================================
        // 7) zip (static)：多源严格配对
        //    与 zipWith 类似，但支持 2~8 个源，用 combinator 合并
        //    任一源完成则整体完成
        // =================================================================
        System.out.println("\n=== 7) Flux.zip: 三源配对 ===");
        Flux<String> names = Flux.just("Alice", "Bob", "Carol");
        Flux<Integer> ages = Flux.just(30, 25, 28);
        Flux<String> cities = Flux.just("Beijing", "Shanghai", "Shenzhen");

        Flux.zip(names, ages, cities)
                .map(t -> t.getT1() + ", " + t.getT2() + "岁, " + t.getT3())
                .subscribe(v -> System.out.println("  " + v));

        // =================================================================
        // 8) elapsed：每个元素附上距上一个元素的时间间隔 (ms)
        //    调试利器——直观看出哪个环节慢
        // =================================================================
        System.out.println("\n=== 8) elapsed: 测量元素间时间间隔 ===");
        CountDownLatch latch6 = new CountDownLatch(1);
        Flux.just(50, 200, 30)
                .concatMap(ms -> Mono.just(ms).delayElement(Duration.ofMillis(ms)))
                .elapsed()
                .subscribe(
                        t -> System.out.println("  +" + t.getT1() + "ms → " + t.getT2()),
                        e -> {},
                        latch6::countDown);
        latch6.await(1, TimeUnit.SECONDS);

        // =================================================================
        // 9) transform：抽取可复用的操作符链
        //    把一段常用的流处理逻辑封装成函数，通过 transform 插入管道
        //    与直接调用的区别：transform 在组装期执行（一次），
        //    transformDeferred 在每次订阅时执行（多次）
        // =================================================================
        System.out.println("\n=== 9) transform: 可复用的操作符链 ===");
        Flux.range(1, 10)
                .transform(E04b_AdvancedOperators::filterAndFormat)  // 插入可复用管道片段
                .subscribe(v -> System.out.println("  " + v));

        // =================================================================
        // 10) cache：缓存已发射的元素，后续订阅者直接重放
        //     把 Cold Publisher 变成"伪 Hot"——第一次订阅执行源，后续从缓存拿
        //     场景：配置加载、token 获取等不想重复执行的操作
        // =================================================================
        System.out.println("\n=== 10) cache: 缓存结果供后续订阅者重放 ===");
        Mono<String> expensive = Mono.fromCallable(() -> {
            System.out.println("  (执行了昂贵的计算)");
            return "computed-value";
        }).cache();     // 首次订阅执行，后续从缓存拿

        expensive.subscribe(v -> System.out.println("  1st: " + v));
        expensive.subscribe(v -> System.out.println("  2nd: " + v));
        expensive.subscribe(v -> System.out.println("  3rd: " + v));
        System.out.println("  (昂贵计算只执行了 1 次)");
    }

    // 可复用管道片段：过滤偶数 + 格式化
    static Flux<String> filterAndFormat(Flux<Integer> source) {
        return source
                .filter(i -> i % 2 == 0)
                .map(i -> "even-" + i);
    }
}
