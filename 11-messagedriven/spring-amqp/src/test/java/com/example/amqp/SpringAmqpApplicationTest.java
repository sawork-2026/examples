package com.example.amqp;

import com.example.amqp.config.RabbitMQConfig;
import com.example.amqp.event.OrderPlacedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class SpringAmqpApplicationTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchangeConfigured() {
        assertEquals("order.exchange", config.orderExchange().getName());
    }

    @Test
    void queuesConfigured() {
        assertEquals("order.inventory", config.inventoryQueue().getName());
        assertEquals("order.payment", config.paymentQueue().getName());
        assertEquals("order.notification", config.notificationQueue().getName());
    }

    @Test
    void jsonMessageConverterConfigured() {
        assertInstanceOf(Jackson2JsonMessageConverter.class,
                config.jsonMessageConverter());
    }

    @Test
    void eventRecordFields() {
        OrderPlacedEvent event = new OrderPlacedEvent("ORD-001", "user-1", 100.0);
        assertEquals("ORD-001", event.orderId());
        assertEquals("user-1", event.userId());
        assertEquals(100.0, event.totalAmount());
    }
}
