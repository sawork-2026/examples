package com.example.jdkflow;

import java.util.concurrent.SubmissionPublisher;

/**
 * 用 JDK 自带的 SubmissionPublisher（java.util.concurrent.Flow.Publisher 的官方实现）
 * 把 0..9 推给 PrintSubscriber。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.example.jdkflow.SubmissionPublisherExample
 */
public class SubmissionPublisherExample {

    public static void main(String... args) throws InterruptedException {
        var publisher = new SubmissionPublisher<Integer>();
        publisher.subscribe(new PrintSubscriber());

        System.out.println("Submitting items...");
        for (int i = 0; i < 10; i++) {
            publisher.submit(i);            // 上游推 10 个；下游通过 request(1) 拉
        }

        Thread.sleep(500);                  // 等异步管道把元素全部投递完
        publisher.close();                  // 触发下游 onComplete
        Thread.sleep(100);
    }
}
