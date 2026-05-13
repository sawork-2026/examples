package com.example.helloworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;

@SpringBootApplication
@EnableIntegration
public class HelloWorldApplication {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(MessageChannel inputChannel, PollableChannel outputChannel) {
        return args -> {
            inputChannel.send(new GenericMessage<>("World"));
            String result = (String) outputChannel.receive(5000).getPayload();
            log.info("Result: {}", result);

            inputChannel.send(new GenericMessage<>("Spring Integration"));
            result = (String) outputChannel.receive(5000).getPayload();
            log.info("Result: {}", result);
        };
    }
}
