package com.traccar.PositionGeofence;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.database.BufferingManager;
import com.traccar.PositionGeofence.handler.BaseEventHandler;
import com.traccar.PositionGeofence.handler.BasePositionHandler;
import com.traccar.PositionGeofence.handler.PostProcessHandler;
import com.traccar.PositionGeofence.handler.network.AcknowledgementHandler;
import com.traccar.PositionGeofence.helper.PositionLogger;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.session.cache.CacheManager;
import io.netty.channel.ChannelHandler.Sharable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;



@Component
@Sharable
public class ProcessingHandler extends ChannelInboundHandlerAdapter implements BufferingManager.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingHandler.class);

    private final CacheManager cacheManager;
    private final PositionLogger positionLogger;
    private final BufferingManager bufferingManager;
    private final List<BasePositionHandler> positionHandlers;
    private final List<BaseEventHandler> eventHandlers;
    private final PostProcessHandler postProcessHandler;

    // Mapa de colas por dispositivo para procesar las posiciones en secuencia.
    private final Map<Long, Queue<Position>> queues = new ConcurrentHashMap<>();

    public ProcessingHandler(Config config,
                             CacheManager cacheManager,
                             PositionLogger positionLogger,
                             List<BasePositionHandler> positionHandlers,
                             List<BaseEventHandler> eventHandlers,
                             PostProcessHandler postProcessHandler) {
        this.cacheManager = cacheManager;
        this.positionLogger = positionLogger;
        this.positionHandlers = positionHandlers;
        this.eventHandlers = eventHandlers;
        this.postProcessHandler = postProcessHandler;
        // Se crea el BufferingManager pasándole la configuración y a este mismo como callback.
        this.bufferingManager = new BufferingManager(config, this);
    }

    // Método para obtener la cola de posiciones de un dispositivo (por deviceId)
    private synchronized Queue<Position> getQueue(long deviceId) {
        return queues.computeIfAbsent(deviceId, k -> new LinkedList<>());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Position position) {
            // Se envía la posición al BufferingManager para agruparla y procesarla en orden.
            bufferingManager.accept(ctx, position);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void onReleased(ChannelHandlerContext ctx, Position position) {
        Queue<Position> queue = getQueue(position.getDeviceId());
        boolean alreadyQueued;
        synchronized (queue) {
            alreadyQueued = !queue.isEmpty();
            queue.offer(position);
        }
        // Si la cola estaba vacía, procesamos esta posición inmediatamente.
        if (!alreadyQueued) {
            try {
                // Nota: Aquí usamos el deviceId (o cualquier identificador único) como key en la caché.
                cacheManager.addDevice(position.getDeviceId(), position.getDeviceId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            processPositionHandlers(ctx, position);
        }
    }

    // Procesa la cadena de "position handlers" (cálculo de atributos, filtros, etc.)
    private void processPositionHandlers(ChannelHandlerContext ctx, Position position) {
        processNextPositionHandler(ctx, position, 0);
    }

    // Itera recursivamente sobre la lista de BasePositionHandler inyectados.
    private void processNextPositionHandler(ChannelHandlerContext ctx, Position position, int index) {
        if (index < positionHandlers.size()) {
            BasePositionHandler handler = positionHandlers.get(index);
            // Cada handler procesa la posición y llama al callback indicando si fue filtrada.
            handler.handlePosition(position, filtered -> {
                if (!filtered) {
                    processNextPositionHandler(ctx, position, index + 1);
                } else {
                    finishedProcessing(ctx, position, true);
                }
            });
        } else {
            //processEventHandlers(ctx, position);
        }
    }

   // Una vez procesados todos los handlers, se recorren los event handlers para analizar la posición y generar eventos.
    // private void processEventHandlers(ChannelHandlerContext ctx, Position position) {
    //     eventHandlers.forEach(handler -> 
    //         handler.analyzePosition(position, event -> 
    //             // Aquí se actualizan los eventos, típicamente a través de NotificationManager.
    //             // Podrías delegar esta llamada a un servicio REST o mantenerla local.
    //             cacheManager.getNotificationManager().updateEvents(java.util.Map.of(event, position))
    //         )
    //     );
    //     finishedProcessing(ctx, position, false);
    // }

    // Post-procesamiento: loguea la posición, envía un ACK y procesa la siguiente posición en cola.
    private void finishedProcessing(ChannelHandlerContext ctx, Position position, boolean filtered) {
        if (!filtered) {
            postProcessHandler.handlePosition(position, ignore -> {
                positionLogger.log(ctx, position);
                ctx.writeAndFlush(new AcknowledgementHandler.EventHandled(position));
                processNextPosition(ctx, position.getDeviceId());
            });
        } else {
            ctx.writeAndFlush(new AcknowledgementHandler.EventHandled(position));
            processNextPosition(ctx, position.getDeviceId());
        }
    }

    // Procesa la siguiente posición en la cola del dispositivo. Si no hay más, elimina la entrada en la caché.
    private void processNextPosition(ChannelHandlerContext ctx, long deviceId) {
        Queue<Position> queue = getQueue(deviceId);
        Position nextPosition;
        synchronized (queue) {
            queue.poll(); // se remueve la posición actual
            nextPosition = queue.peek();
        }
        if (nextPosition != null) {
            processPositionHandlers(ctx, nextPosition);
        } else {
            cacheManager.removeDevice(deviceId, deviceId);
        }
    }
}