package com.example.amqp;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

@Configuration
public class AmqpIntegrationConfig {

    public static final String QUEUE_NAME = "si.demo.queue";

    @Bean
    public Queue demoQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    // --- Outbound: Channel → RabbitMQ ---

    @Bean
    public MessageChannel toRabbitChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "toRabbitChannel")
    public MessageHandler amqpOutbound(AmqpTemplate amqpTemplate) {
        AmqpOutboundEndpoint endpoint = new AmqpOutboundEndpoint(amqpTemplate);
        endpoint.setRoutingKey(QUEUE_NAME);
        endpoint.setExpectReply(false);
        return endpoint;
    }

    // --- Inbound: RabbitMQ → Channel ---

    @Bean
    public MessageChannel fromRabbitChannel() {
        return new DirectChannel();
    }

    @Bean
    public AmqpInboundChannelAdapter amqpInbound(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(QUEUE_NAME);
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
        adapter.setOutputChannel(fromRabbitChannel());
        return adapter;
    }
}
