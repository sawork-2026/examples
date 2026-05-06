package com.example.amqp;

import com.example.amqp.publisher.OrderPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAmqpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAmqpApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(OrderPublisher orderPublisher) {
        return args -> {
            System.out.println("=== Spring AMQP Demo ===");
            System.out.println();
            orderPublisher.placeOrder("ORD-001", "user-zhangsan", 299.00);
            Thread.sleep(500);
            orderPublisher.placeOrder("ORD-002", "user-lisi", 1599.00);
        };
    }
}
