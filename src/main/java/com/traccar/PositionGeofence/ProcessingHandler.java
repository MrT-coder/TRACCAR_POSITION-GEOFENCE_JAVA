package com.traccar.PositionGeofence;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.handler.network.AcknowledgementHandler;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.servicio.PositionService;
import com.traccar.PositionGeofence.session.cache.CacheManager;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ProcessingHandler para el microservicio de position/geofence.
 * 
 * Responsabilidades:
 * - Recibir posiciones y encolarlas por dispositivo.
 * - Procesar cada posición en orden mediante una cadena de procesamiento simplificada.
 * - Persistir la posición en MongoDB a través de PositionService.
 * - Publicar la posición a RabbitMQ para que el microservicio Events la procese.
 * - Registrar la posición mediante PositionLogger.
 * 
 * La lógica de manejo de eventos, notificaciones y gestión compleja de dispositivos se elimina,
 * delegándose a los microservicios Device y Events.
 */
@Component
public class ProcessingHandler extends ChannelInboundHandlerAdapter implements BufferingManager.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingHandler.class);

    private final CacheManager cacheManager;
    private final Position positionLogger;
    private final BufferingManager bufferingManager;
    private final PositionService positionService;
    private final RabbitTemplate rabbitTemplate;

    // Map para encolar posiciones por deviceId
    private final Map<Long, Queue<Position>> queues = new ConcurrentHashMap<>();

    public ProcessingHandler(CacheManager cacheManager,
                             Position positionLogger,
                             BufferingManager bufferingManager,
                             PositionService positionService,
                             RabbitTemplate rabbitTemplate) {
        this.cacheManager = cacheManager;
        this.positionLogger = positionLogger;
        this.bufferingManager = bufferingManager;
        this.positionService = positionService;
        this.rabbitTemplate = rabbitTemplate;
    }

    private synchronized Queue<Position> getQueue(long deviceId) {
        return queues.computeIfAbsent(deviceId, k -> new LinkedList<>());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Position position) {
            // Se delega a BufferingManager para procesar posiciones de forma ordenada
            bufferingManager.accept(ctx, position);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * Callback de BufferingManager cuando una posición se libera para procesarse.
     */
    @Override
    public void onReleased(ChannelHandlerContext ctx, Position position) {
        Queue<Position> queue = getQueue(position.getDeviceId());
        boolean alreadyQueued;
        synchronized (queue) {
            alreadyQueued = !queue.isEmpty();
            queue.offer(position);
        }
        if (!alreadyQueued) {
            try {
                cacheManager.addDevice(position.getDeviceId(), position.getDeviceId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Procesa la posición
            processPositionHandlers(ctx, position);
        }
    }

    /**
     * Procesa la posición a través de la cadena de handlers específica para posiciones.
     * En esta versión simplificada, se salta la cadena compleja y se procede al procesamiento final.
     */
    private void processPositionHandlers(ChannelHandlerContext ctx, Position position) {
        processFinalPosition(ctx, position);
    }

    /**
     * Procesamiento final de la posición:
     * - Persiste la posición en MongoDB a través de PositionService.
     * - Publica la posición a RabbitMQ para el microservicio Events.
     */
    private void processFinalPosition(ChannelHandlerContext ctx, Position position) {
        // Persiste la posición en MongoDB
        positionService.savePosition(position);
        LOGGER.info("Posición guardada en MongoDB para deviceId {}.", position.getDeviceId());

        // Publica la posición a RabbitMQ (exchange "eventsExchange", routing key "position.update")
        rabbitTemplate.convertAndSend("eventsExchange", "position.update", position);
        LOGGER.info("Posición publicada a RabbitMQ para el microservicio Events.");

        finishedProcessing(ctx, position, false);
    }

    /**
     * Finaliza el procesamiento de la posición:
     * - Envía una respuesta de acuse de recibo.
     * - Registra la posición mediante PositionLogger.
     * - Procesa la siguiente posición en cola para el dispositivo.
     */
    private void finishedProcessing(ChannelHandlerContext ctx, Position position, boolean filtered) {
        ctx.writeAndFlush(new AcknowledgementHandler.EventHandled(position));
        positionLogger.log(ctx, position);
        processNextPosition(ctx, position.getDeviceId());
    }

    /**
     * Procesa la siguiente posición en cola para el dispositivo.
     */
    private void processNextPosition(ChannelHandlerContext ctx, long deviceId) {
        Queue<Position> queue = getQueue(deviceId);
        Position nextPosition;
        synchronized (queue) {
            queue.poll(); // Elimina la posición actual
            nextPosition = queue.peek();
        }
        if (nextPosition != null) {
            processPositionHandlers(ctx, nextPosition);
        } else {
            cacheManager.removeDevice(deviceId, deviceId);
        }
    }
}