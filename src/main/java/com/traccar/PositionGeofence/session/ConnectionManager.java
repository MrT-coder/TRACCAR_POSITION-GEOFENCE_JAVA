package com.traccar.PositionGeofence.session;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.servicio.PositionService;
import com.traccar.PositionGeofence.session.cache.CacheManager;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Versión adaptada de ConnectionManager para el microservicio de position/geofence,
 * integrando manejo de cache mediante CacheManager.
 */
@Component
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    @Value("${device.timeout:30}")
    private long deviceTimeout;

    @Value("${web.show.unknown.devices:false}")
    private boolean showUnknownDevices;

    // Gestión mínima de sesiones
    private final Map<Long, DeviceSession> sessionsByDeviceId = new ConcurrentHashMap<>();
    private final Map<ConnectionKey, DeviceSession> sessionsByEndpoint = new ConcurrentHashMap<>();

    private final Timer timer;
    private final PositionService positionService;
    private final RabbitTemplate rabbitTemplate;
    private final CacheManager cacheManager;

    public ConnectionManager(Timer timer, PositionService positionService, RabbitTemplate rabbitTemplate, CacheManager cacheManager) {
        this.timer = timer;
        this.positionService = positionService;
        this.rabbitTemplate = rabbitTemplate;
        this.cacheManager = cacheManager;
    }

    /**
     * Retorna la sesión asociada a un dispositivo, si existe.
     */
    public DeviceSession getDeviceSession(long deviceId) {
        return sessionsByDeviceId.get(deviceId);
    }

    /**
     * Obtiene o crea una sesión para un dispositivo basado en la conexión.
     * Se asume que la lógica de lookup y registro de dispositivos se maneja en el microservicio Device,
     * por lo que aquí solo se correlaciona la conexión (endpoint) con un deviceId ya existente.
     * @throws Exception 
     */
    public DeviceSession getDeviceSession(Protocol protocol, Channel channel, SocketAddress remoteAddress, String... uniqueIds) throws Exception {
        ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
        if (uniqueIds != null && uniqueIds.length > 0) {
            try {
                long deviceId = Long.parseLong(uniqueIds[0]);
                DeviceSession session = sessionsByDeviceId.get(deviceId);
                if (session == null) {
                    session = new DeviceSession(deviceId, uniqueIds[0], "", protocol, channel, remoteAddress);
                    sessionsByDeviceId.put(deviceId, session);
                    sessionsByEndpoint.put(connectionKey, session);
                    // Agregamos al cache la nueva conexión
                    cacheManager.addDevice(deviceId, connectionKey);
                }
                return session;
            } catch (NumberFormatException e) {
                LOGGER.warn("UniqueId {} no se pudo convertir a deviceId", uniqueIds[0], e);
                return null;
            }
        }
        return null;
    }

    /**
     * Llamado cuando se recibe una nueva posición.
     * Persiste la posición en MongoDB y la publica a RabbitMQ.
     */
    public void updatePosition(Position position) {
        positionService.savePosition(position);
        LOGGER.info("Posición guardada para deviceId {}: {}", position.getDeviceId(), position);
        rabbitTemplate.convertAndSend("eventsExchange", "position.routing.key", position);
        LOGGER.info("Posición publicada a RabbitMQ para Events.");
    }

    /**
     * Se invoca cuando un dispositivo se desconecta; se elimina la sesión y se limpia el cache.
     */
    public void deviceDisconnected(Channel channel) {
        SocketAddress remoteAddress = channel.remoteAddress();
        if (remoteAddress != null) {
            ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
            DeviceSession session = sessionsByEndpoint.remove(connectionKey);
            if (session != null) {
                sessionsByDeviceId.remove(session.getDeviceId());
                cacheManager.removeDevice(session.getDeviceId(), connectionKey);
                LOGGER.info("Sesión removida para deviceId {} por desconexión.", session.getDeviceId());
            }
        }
    }

    /**
     * Programa un timeout para considerar un dispositivo inactivo.
     */
    public void scheduleInactivityTimeout(long deviceId) {
        Timeout timeout = timer.newTimeout(t -> {
            sessionsByDeviceId.remove(deviceId);
            LOGGER.info("Timeout alcanzado para deviceId {}. Se considera inactivo.", deviceId);
            // Opcional: enviar mensaje a RabbitMQ para notificar inactividad.
        }, deviceTimeout, TimeUnit.SECONDS);
        // En esta versión simplificada no almacenamos el Timeout para cancelarlo posteriormente.
    }
}