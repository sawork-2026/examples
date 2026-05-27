package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

/**
 * Cold vs Hot Publisher 对比。
 *   - Cold：订阅时才开始产生数据，每个订阅者都从头收到完整序列
 *   - Hot：不管有无订阅者持续产生数据，晚订阅的会 miss 掉之前的元素
 *
 * 典型 Cold：HTTP 请求、数据库查询（每次订阅 = 一次新请求）
 * 典型 Hot：鼠标事件、股票行情、Kafka 主题（事件持续发生，不等订阅者）
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E05_ColdVsHot
 */
public class E05_ColdVsHot {

    public static void main(String[] args) throws InterruptedException {
        coldDemo();
        hotDemo();
    }

    static void coldDemo() throws InterruptedException {
        // Cold Publisher: Flux.range 是 cold 的——每次 subscribe 都重新从 1 开始
        // A 先订阅，250ms 后 B 再订阅，两者都完整收到 1,2,3,4,5
        System.out.println("\n=== COLD: Flux.range(1, 5).delayElements(100ms) ===");
        Flux<Integer> cold = Flux.range(1, 5).delayElements(Duration.ofMillis(100));

        CountDownLatch latch = new CountDownLatch(2);
        cold.subscribe(i -> System.out.println("  A received: " + i), e -> {}, latch::countDown);

        Thread.sleep(250); // A 已经收到 1,2 后，B 才订阅
        cold.subscribe(i -> System.out.println("  B received: " + i), e -> {}, latch::countDown);

        latch.await(3, TimeUnit.SECONDS);
        // 观察：A 和 B 都收到 1,2,3,4,5 —— 各自独立的数据源
    }

    static void hotDemo() throws InterruptedException {
        // Hot Publisher: .share() 把 cold 的 interval 变成 hot
        // share() = publish().refCount() —— 第一个订阅者触发源头，后续订阅者共享同一条流
        // A 先订阅拿到 0,1,2,...  350ms 后 B 订阅，只能拿到 3,4,5,...
        System.out.println("\n=== HOT: Flux.interval(100ms).share() ===");
        Flux<Long> hot = Flux.interval(Duration.ofMillis(100)).take(8).share();

        hot.subscribe(i -> System.out.println("  A received: " + i));

        Thread.sleep(350); // A 已经收到 0,1,2
        System.out.println("  -- B subscribes now --");
        hot.subscribe(i -> System.out.println("  B received: " + i));

        Thread.sleep(1000);
        // 观察：A 收 0-7；B 大约从 3 开始，miss 了前面的
    }
}
