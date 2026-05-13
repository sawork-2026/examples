package com.example.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
@EnableIntegration
public class AmqpApplication {

    private static final Logger log = LoggerFactory.getLogger(AmqpApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AmqpApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(MessageChannel toRabbitChannel) {
        return args -> {
            for (int i = 1; i <= 3; i++) {
                String msg = "Order-" + i;
                log.info("Sending to RabbitMQ: {}", msg);
                toRabbitChannel.send(new GenericMessage<>(msg));
            }
        };
    }
}
