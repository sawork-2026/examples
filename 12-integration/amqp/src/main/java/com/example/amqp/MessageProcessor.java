package com.example.amqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    @ServiceActivator(inputChannel = "fromRabbitChannel")
    public void handle(String payload) {
        log.info("Received from RabbitMQ: {}", payload);
    }
}
