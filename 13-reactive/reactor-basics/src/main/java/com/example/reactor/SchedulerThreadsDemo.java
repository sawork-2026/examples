package com.example.reactor;

import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * 把"上下游"实际线程名打印出来，看 publishOn / subscribeOn 各自影响什么。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.SchedulerThreadsDemo
 */
public class SchedulerThreadsDemo {

    static void log(String stage, Object value) {
        System.out.printf("  [%s] %s -> %s%n",
                Thread.currentThread().getName(), stage, value);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("\n=== 1) 不加任何 Scheduler：全在 main ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(50);
        System.out.println("\n=== 2) subscribeOn(boundedElastic)：影响源头与上游 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println("\n=== 3) publishOn(parallel)：切换下游线程 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .publishOn(Schedulers.parallel())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(100);
        System.out.println("\n=== 4) 同时使用：subscribeOn 决定起点，publishOn 切换中段 ===");
        Flux.range(1, 3)
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .map(i -> { log("map2", i); return i + 1; })
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(200);
        System.out.println("\n=== 5) 多个 subscribeOn 只有最靠近源头的生效 ===");
        Flux.range(1, 3)
                .subscribeOn(Schedulers.parallel())          // 这一个会胜出
                .map(i -> { log("map1", i); return i * 10; })
                .subscribeOn(Schedulers.boundedElastic())    // 被忽略
                .subscribe(v -> log("subscribe", v));

        TimeUnit.MILLISECONDS.sleep(200);
    }
}
