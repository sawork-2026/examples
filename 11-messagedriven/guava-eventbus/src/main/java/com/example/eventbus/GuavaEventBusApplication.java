package com.example.eventbus;

import com.example.eventbus.publisher.OrderService;
import com.google.common.eventbus.EventBus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GuavaEventBusApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuavaEventBusApplication.class, args);
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean
    public CommandLineRunner demo(OrderService orderService) {
        return args -> {
            System.out.println("=== Guava EventBus Demo ===");
            System.out.println();
            orderService.placeOrder("ORD-001", "user-zhangsan", 299.00);
            System.out.println();
            orderService.placeOrder("ORD-002", "user-lisi", 1599.00);
        };
    }
}
