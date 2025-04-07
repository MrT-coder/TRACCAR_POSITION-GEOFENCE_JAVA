package com.traccar.PositionGeofence.mensajeria;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PositionMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendPositionMessage(Object positionData) {
        // Enviar el mensaje usando el exchange y routing key configurados
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.POSITION_EXCHANGE,
                RabbitMQConfig.POSITION_ROUTING_KEY,
                positionData
        );
    }
}
