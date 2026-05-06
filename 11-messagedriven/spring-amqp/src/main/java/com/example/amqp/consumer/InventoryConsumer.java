package com.example.amqp.consumer;

import com.example.amqp.config.RabbitMQConfig;
import com.example.amqp.event.OrderPlacedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY)
    public void handleOrder(OrderPlacedEvent event) {
        System.out.println("[Inventory] 扣减库存 — 订单: " + event.orderId());
    }
}
