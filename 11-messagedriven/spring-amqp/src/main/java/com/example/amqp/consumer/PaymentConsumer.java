package com.example.amqp.consumer;

import com.example.amqp.config.RabbitMQConfig;
import com.example.amqp.event.OrderPlacedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT)
    public void handleOrder(OrderPlacedEvent event) {
        System.out.println("[Payment] 发起支付 — 订单: " + event.orderId()
                + ", 金额: " + event.totalAmount());
    }
}
