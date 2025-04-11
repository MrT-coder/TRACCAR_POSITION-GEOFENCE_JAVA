package com.traccar.PositionGeofence.broadcast;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementación de BroadcastService para enviar mensajes mediante RabbitMQ.
 * Extiende la funcionalidad de BaseBroadcastService y usa el RabbitTemplate para difundir mensajes.
 */
@Service
public class RabbitBroadcastServiceImpl extends BaseBroadcastService implements BroadcastService {

    // Se usa RabbitTemplate para enviar los mensajes a un exchange.
    private final RabbitTemplate rabbitTemplate;

    // Se inyectan desde la configuración; si no se definen en application.yml, se usan valores por defecto.
    @Value("${broadcast.exchange:traccarExchange}")
    private String exchange;

    @Value("${broadcast.routing-key:broadcast}")
    private String routingKey;

    public RabbitBroadcastServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    protected void sendMessage(BroadcastMessage message) {
        // Envía el mensaje al exchange definido con la routing key configurada.
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @Override
    public void start() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }
    
    // Los demás métodos de la clase se heredan de BaseBroadcastService y no necesitan cambios
    // a menos que quieras adaptar lógicamente algún comportamiento a tu arquitectura.
}