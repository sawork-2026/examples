package com.example.reactor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * Fast producer (every 1ms) + slow consumer (50ms each) → 必然背压。
 * 用同一组数据，分别跑四种策略，对照输出。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.BackpressureStrategiesDemo
 */
public class BackpressureStrategiesDemo {

    static Flux<Long> fastProducer() {
        return Flux.interval(Duration.ofMillis(1))
                .take(50);
    }

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
        slowConsume("BUFFER (size=10)",
                fastProducer().onBackpressureBuffer(10,
                        dropped -> System.out.println("  buffer overflow drop: " + dropped)));

        slowConsume("DROP",
                fastProducer().onBackpressureDrop(
                        dropped -> System.out.println("  dropped: " + dropped)));

        slowConsume("LATEST",
                fastProducer().onBackpressureLatest());

        slowConsume("ERROR",
                fastProducer().onBackpressureError());
    }
}
