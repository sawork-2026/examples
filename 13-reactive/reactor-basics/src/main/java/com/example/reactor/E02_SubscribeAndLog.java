package com.example.reactor;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

/**
 * 演示 subscribe() 的几种重载 + .log() 打印完整信号生命周期。
 * 对应 slides "Subscribing to a Stream" / "The Flow of Elements" / "Subscriber" 三页。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E02_SubscribeAndLog
 */
public class E02_SubscribeAndLog {

    public static void main(String[] args) {

        // =================================================================
        // 1) .log() 算子：拦截并打印 Reactive Streams 协议的全部内部信号
        //    观察输出中的信号顺序：
        //    onSubscribe → request(unbounded) → onNext(1) → ... → onComplete
        //    这正是 Reactive Streams 规范定义的生命周期
        // =================================================================
        System.out.println("=== 1) .log() 观察完整信号 ===");
        System.out.println("注意观察: onSubscribe → request(unbounded) → onNext × 4 → onComplete\n");
        Flux.just(1, 2, 3, 4)
                .log()
                .subscribe(v -> System.out.println("  received: " + v));

        // =================================================================
        // 2) subscribe() 的几种重载形式
        //    Reactor 提供从无参到三参的 lambda 快捷方式
        // =================================================================
        System.out.println("\n=== 2) subscribe() 的几种重载 ===");

        // 无参 subscribe(): 触发数据流动，但不处理任何元素
        // .log("no-arg") 仍能看到内部信号
        System.out.println("\n--- subscribe() 无参：启动但不处理 ---");
        Flux.just("a", "b").log("no-arg").subscribe();

        // 一个参数: 只处理 onNext 信号（每个元素）
        System.out.println("\n--- subscribe(consumer) ---");
        Flux.just("a", "b").subscribe(v -> System.out.println("  element: " + v));

        // 两个参数: onNext + onError
        // 这里故意除以 0 触发 ArithmeticException，观察错误如何传播
        System.out.println("\n--- subscribe(consumer, errorConsumer) ---");
        Flux.just(1, 2, 0)
                .map(i -> 10 / i)
                .subscribe(
                        v -> System.out.println("  result: " + v),
                        e -> System.out.println("  error: " + e.getMessage()));

        // 三个参数: onNext + onError + onComplete
        // onComplete 在所有元素发完后触发（与 onError 互斥）
        System.out.println("\n--- subscribe(consumer, errorConsumer, completeConsumer) ---");
        Flux.just("x", "y", "z")
                .subscribe(
                        v -> System.out.println("  element: " + v),
                        e -> System.out.println("  error: " + e),
                        () -> System.out.println("  completed!"));

        // =================================================================
        // 3) 完整 Subscriber 实现
        //    上面的 lambda 形式是语法糖——Reactor 内部帮你创建了一个 Subscriber，
        //    其 onSubscribe 默认调用 request(Long.MAX_VALUE)（即"无限拉"，放弃背压）。
        //    这里手动展开，让你看到完整的 Subscriber 接口长什么样。
        // =================================================================
        System.out.println("\n=== 3) 完整 Subscriber (lambda 的展开形式) ===");
        System.out.println("lambda subscribe 默认 request(Long.MAX_VALUE)，这里手动展开看效果：\n");
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log("full-subscriber")
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        // 关键: request(Long.MAX_VALUE) = "我都要，不限速"
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer i) {
                        elements.add(i);
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        System.out.println("  collected: " + elements);
                    }
                });
    }
}
