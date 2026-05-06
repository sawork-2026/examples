package com.example.amqp.consumer;

import com.example.amqp.config.RabbitMQConfig;
import com.example.amqp.event.OrderPlacedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void handleOrder(OrderPlacedEvent event) {
        System.out.println("[Notification] 发送通知 — 用户: " + event.userId()
                + ", 订单: " + event.orderId());
    }
}
