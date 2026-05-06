package com.example.amqp.publisher;

import com.example.amqp.config.RabbitMQConfig;
import com.example.amqp.event.OrderPlacedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void placeOrder(String orderId, String userId, double totalAmount) {
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, userId, totalAmount);
        System.out.println("[Publisher] 发布订单事件: " + orderId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
