package com.example.reactor;

import java.util.ArrayList;
import java.util.List;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

/**
 * 用自定义 Subscriber 的 request(n) 控制拉取速率——协议层面的背压。
 * 对应 slides "Backpressure" 那页：把 request(Long.MAX_VALUE) 换成有限的 n。
 *
 * 核心要点：
 *   - 下游不发 request(n) → 上游就不推数据 → 这本身就是背压
 *   - request 是累加的：request(3) + request(2) = 可以收 5 个
 *   - n 的大小决定吞吐量 vs 内存占用的权衡
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.reactor.E03_BackpressureRequest
 */
public class E03_BackpressureRequest {

    public static void main(String[] args) {

        // =================================================================
        // 场景 1: request(1) — 一次只要一个，处理完再要下一个
        // 这是最朴素的背压形式，等价于 jdk-flow-demo 中 PrintSubscriber 的写法
        // 观察 .log() 输出：request(1) → onNext → request(1) → onNext → ...
        // =================================================================
        System.out.println("=== 1) request(1)：一次一个，最朴素的背压 ===\n");
        Flux.range(1, 5)
                .log("req-1")
                .subscribe(new Subscriber<>() {
                    private Subscription s;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(1);               // 初始需求：先要 1 个
                    }

                    @Override
                    public void onNext(Integer i) {
                        System.out.println("  processed: " + i);
                        s.request(1);               // 处理完一个，再要 1 个
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        System.out.println("  complete\n");
                    }
                });

        // =================================================================
        // 场景 2: request(2) — 两个两个拉，slides 中的原始示例
        // 观察 .log() 输出节奏：request(2) → onNext × 2 → request(2) → ...
        // 相比 request(1)，批量更大 = 更少的 request 调用 = 更高吞吐
        // =================================================================
        System.out.println("=== 2) request(2)：两个两个拉 ===\n");
        System.out.println("观察 .log() 输出: request(2) → onNext × 2 → request(2) → ...\n");
        List<Integer> elements = new ArrayList<>();
        Flux.just(1, 2, 3, 4)
                .log("req-2")
                .subscribe(new Subscriber<>() {
                    private Subscription s;
                    private int count;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(2);               // 起步要 2 个
                    }

                    @Override
                    public void onNext(Integer i) {
                        elements.add(i);
                        if (++count % 2 == 0) {
                            s.request(2);           // 每收满 2 个再要 2 个
                        }
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        System.out.println("  collected: " + elements + "\n");
                    }
                });

        // =================================================================
        // 场景 3: request(3) — 批量更大，观察"不整除"时的尾巴处理
        // 10 个元素按 3 个一批拉：3 + 3 + 3 + 1（最后一批不足 3 个直接 onComplete）
        // =================================================================
        System.out.println("=== 3) request(3)：批量更大，吞吐更高 ===\n");
        List<Integer> batch3 = new ArrayList<>();
        Flux.range(1, 10)
                .log("req-3")
                .subscribe(new Subscriber<>() {
                    private Subscription s;
                    private int count;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.s = s;
                        s.request(3);
                    }

                    @Override
                    public void onNext(Integer i) {
                        batch3.add(i);
                        if (++count % 3 == 0) {
                            System.out.println("  batch received: " + batch3.subList(batch3.size() - 3, batch3.size()));
                            s.request(3);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        System.out.println("  all: " + batch3);
                    }
                });
    }
}
