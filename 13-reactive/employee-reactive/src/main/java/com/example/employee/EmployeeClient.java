package com.example.employee;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.FlowAdapters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 服务启动后自动运行：用 WebClient 订阅 /employees 流式端点，
 * 手动实现 Subscriber 以 request(2) 控制背压。
 */
@Component
public class EmployeeClient implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(500); // 等服务就绪

        CountDownLatch latch = new CountDownLatch(1);

        WebClient.create("http://localhost:8080")
                .get().uri("/employees")
                .retrieve()
                .bodyToFlux(Employee.class)
                .subscribe(FlowAdapters.toSubscriber(new Subscriber<Employee>() {
                    private Subscription subscription;
                    private int count = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(2);
                        System.out.println("[Client] requested 2");
                    }

                    @Override
                    public void onNext(Employee e) {
                        count++;
                        System.out.println("[Client] received: " + e);
                        if (count % 2 == 0) {
                            subscription.request(2);
                            System.out.println("[Client] requested 2");
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("[Client] error: " + t.getMessage());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("[Client] complete");
                        latch.countDown();
                    }
                }));

        latch.await(10, TimeUnit.SECONDS);
    }
}
