package com.example.jdkflow;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

/**
 * 手写一个 Flow.Subscriber，用最朴素的方式演示背压：
 * 每收到一个元素就 request(1) 再要一个——速率由下游主导。
 */
public class PrintSubscriber implements Subscriber<Integer> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        s.request(1);                       // 阀门开一格：先要 1 个
    }

    @Override
    public void onNext(Integer item) {
        System.out.println("Received: " + item);
        subscription.request(1);            // 处理完一个，再要 1 个
    }

    @Override
    public void onError(Throwable e) {
        System.out.println("Error: " + e.getMessage());
    }

    @Override
    public void onComplete() {
        System.out.println("PrintSubscriber complete");
    }
}
