package com.traccar.PositionGeofence.mensajeria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String POSITION_QUEUE = "position.queue";
    public static final String POSITION_EXCHANGE = "position.exchange";
    public static final String POSITION_ROUTING_KEY = "position.key";

    @Bean
    public Queue positionQueue() {
        return new Queue(POSITION_QUEUE, true);
    }

    @Bean
    public TopicExchange positionExchange() {
        return new TopicExchange(POSITION_EXCHANGE);
    }

    @Bean
    public Binding positionBinding(Queue positionQueue, TopicExchange positionExchange) {
        return BindingBuilder.bind(positionQueue).to(positionExchange).with(POSITION_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
