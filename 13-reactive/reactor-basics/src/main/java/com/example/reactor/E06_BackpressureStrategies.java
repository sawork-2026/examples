package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * 当上游产生速度 >> 下游消费速度时，四种背压策略的对比。
 *
 * 场景：fast producer 每 1ms 发一个（共 50 个），slow consumer 每个要 50ms 处理。
 *       下游根本来不及——看不同策略如何应对。
 *
 *   BUFFER:  缓冲溢出的数据，超出 maxSize 后报错/丢弃
 *   DROP:    来不及处理的直接丢掉，下游只拿到它能处理的
 *   LATEST:  只保留最新的一个，旧的被覆盖
 *   ERROR:   直接 onError，快速失败
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E06_BackpressureStrategies
 */
public class E06_BackpressureStrategies {

    // 快速生产者：每 1ms 发一个，共 50 个
    static Flux<Long> fastProducer() {
        return Flux.interval(Duration.ofMillis(1))
                .take(50);
    }

    // 慢消费者：每个元素花 50ms 处理
    // publishOn(scheduler, prefetch=1) 限制预取为 1，强制触发背压
    static void slowConsume(String label, Flux<Long> source) throws InterruptedException {
        System.out.println("\n=== " + label + " ===");
        CountDownLatch done = new CountDownLatch(1);
        source.publishOn(Schedulers.boundedElastic(), 1)
                .subscribe(
                        i -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(50);
                            } catch (InterruptedException ignored) {
                            }
                            System.out.println("  consumed: " + i);
                        },
                        err -> {
                            System.out.println("  onError: " + err);
                            done.countDown();
                        },
                        () -> {
                            System.out.println("  onComplete");
                            done.countDown();
                        });
        done.await(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {

        // BUFFER: 缓冲最多 10 个，溢出时回调 dropped consumer
        slowConsume("BUFFER (size=10)",
                fastProducer().onBackpressureBuffer(10,
                        dropped -> System.out.println("  buffer overflow drop: " + dropped)));

        // DROP: 下游没 request 时直接丢弃，回调通知被丢的元素
        slowConsume("DROP",
                fastProducer().onBackpressureDrop(
                        dropped -> System.out.println("  dropped: " + dropped)));

        // LATEST: 永远只保留最新的一个，下游下次 request 时拿到最新值
        slowConsume("LATEST",
                fastProducer().onBackpressureLatest());

        // ERROR: 下游来不及就直接 onError(IllegalStateException)
        slowConsume("ERROR",
                fastProducer().onBackpressureError());
    }
}
