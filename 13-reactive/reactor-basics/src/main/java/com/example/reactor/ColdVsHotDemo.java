package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;

/**
 * 同一个 Publisher，两个订阅者：
 *   - Cold：每个订阅者都从头收到完整数据
 *   - Hot：订阅者只能收到订阅之后产生的数据
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.ColdVsHotDemo
 */
public class ColdVsHotDemo {

    public static void main(String[] args) throws InterruptedException {
        coldDemo();
        hotDemo();
    }

    static void coldDemo() throws InterruptedException {
        System.out.println("\n=== COLD: Flux.range(1, 5).delayElements(100ms) ===");
        Flux<Integer> cold = Flux.range(1, 5).delayElements(Duration.ofMillis(100));

        CountDownLatch latch = new CountDownLatch(2);
        cold.subscribe(i -> System.out.println("  A received: " + i), e -> {}, latch::countDown);

        Thread.sleep(250); // 让 A 已经收到 1-2 后，B 才订阅
        cold.subscribe(i -> System.out.println("  B received: " + i), e -> {}, latch::countDown);

        latch.await(3, TimeUnit.SECONDS);
        // 期望：A 和 B 都收到 1,2,3,4,5（各自独立的源）
    }

    static void hotDemo() throws InterruptedException {
        System.out.println("\n=== HOT: Flux.interval(100ms).share() ===");
        Flux<Long> hot = Flux.interval(Duration.ofMillis(100)).take(8).share();

        hot.subscribe(i -> System.out.println("  A received: " + i));

        Thread.sleep(350); // 让 A 已经收到 0,1,2 后 B 才订阅
        System.out.println("  -- B subscribes now --");
        hot.subscribe(i -> System.out.println("  B received: " + i));

        Thread.sleep(1000);
        // 期望：A 收 0-7；B 大约从 3 开始往后，miss 掉前面
    }
}
