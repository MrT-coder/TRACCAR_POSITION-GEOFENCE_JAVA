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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Versión simplificada de ConnectionManager para el microservicio de position/geofence.
 * 
 * Responsabilidades:
 * - Gestiona mínimamente las sesiones para asociar una posición a un dispositivo.
 * - Recibe posiciones, las persiste en MongoDB mediante PositionService,
 *   y las envía a RabbitMQ para que el microservicio Events las consuma.
 *
 * Nota: Se han eliminado funcionalidades complejas de gestión de dispositivos, listeners,
 *       y notificaciones, que se delegarán al microservicio Device o al de Events.
 */
@Component
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    // Tiempo de inactividad (en segundos) antes de considerar inactivo un dispositivo
    @Value("${device.timeout:30}")
    private long deviceTimeout;

    // Si se deben mostrar dispositivos desconocidos (no utilizado en esta versión)
    @Value("${web.show.unknown.devices:false}")
    private boolean showUnknownDevices;

    // Estructuras mínimas para gestionar sesiones
    private final Map<Long, DeviceSession> sessionsByDeviceId = new ConcurrentHashMap<>();

    // Para correlacionar la conexión (canal y dirección) con la sesión; se usa ConnectionKey
    private final Map<ConnectionKey, DeviceSession> sessionsByEndpoint = new ConcurrentHashMap<>();

    // Timer para gestionar timeouts de sesión (si se requiere)
    private final Timer timer;

    // Servicio para persistir posiciones en MongoDB (implementado con Spring Data, por ejemplo)
    private final PositionService positionService;

    // RabbitTemplate para publicar posiciones a RabbitMQ y enviar mensajes al microservicio Events
    private final RabbitTemplate rabbitTemplate;

    public ConnectionManager(Timer timer, PositionService positionService, RabbitTemplate rabbitTemplate) {
        this.timer = timer;
        this.positionService = positionService;
        this.rabbitTemplate = rabbitTemplate;
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
     */
    public DeviceSession getDeviceSession(Protocol protocol, Channel channel, SocketAddress remoteAddress, String... uniqueIds) {
        // Se crea una clave de conexión a partir del canal y dirección remota.
        ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
        // Se asume que el primer uniqueId corresponde al deviceId (convertido a long), o se obtiene de otra fuente.
        // En este ejemplo, si no se encuentra la sesión, se retorna null.
        if (uniqueIds != null && uniqueIds.length > 0) {
            try {
                long deviceId = Long.parseLong(uniqueIds[0]);
                DeviceSession session = sessionsByDeviceId.get(deviceId);
                if (session == null) {
                    session = new DeviceSession(deviceId, uniqueIds[0], "", protocol, channel, remoteAddress);
                    sessionsByDeviceId.put(deviceId, session);
                    sessionsByEndpoint.put(connectionKey, session);
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
     * Se persiste la posición en MongoDB y se publica un mensaje a RabbitMQ para el microservicio Events.
     */
    public void updatePosition(Position position) {
        // Persiste la posición mediante el PositionService.
        positionService.savePosition(position);
        LOGGER.info("Posición guardada para deviceId {}: {}", position.getDeviceId(), position);

        // Publica la posición a RabbitMQ.
        // Se asume que existe un exchange "eventsExchange" y se utiliza una routing key configurada.
        rabbitTemplate.convertAndSend("eventsExchange", "position.routing.key", position);
        LOGGER.info("Posición publicada a RabbitMQ para Events.");
    }

    /**
     * Llamado cuando se detecta que un dispositivo se desconectó.
     * Se limpia la sesión asociada.
     */
    public void deviceDisconnected(Channel channel) {
        SocketAddress remoteAddress = channel.remoteAddress();
        if (remoteAddress != null) {
            ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
            DeviceSession session = sessionsByEndpoint.remove(connectionKey);
            if (session != null) {
                sessionsByDeviceId.remove(session.getDeviceId());
                LOGGER.info("Sesión removida para deviceId {} por desconexión.", session.getDeviceId());
            }
        }
    }

    /**
     * Actualiza el estado del dispositivo en cuanto a inactividad mediante timeout.
     * Si se supera el tiempo configurado, se puede marcar el dispositivo como inactivo o desconocido.
     * (Esta lógica se puede simplificar o incluso eliminar si la gestión de estado se delega al microservicio Device.)
     */
    public void scheduleInactivityTimeout(long deviceId) {
        Timeout timeout = timer.newTimeout(t -> {
            // Si se cumple el timeout, se elimina la sesión y se registra el dispositivo como inactivo.
            sessionsByDeviceId.remove(deviceId);
            LOGGER.info("Timeout alcanzado para deviceId {}. Se considera inactivo.", deviceId);
            // Aquí se podría enviar un mensaje adicional a RabbitMQ si es necesario.
        }, deviceTimeout, TimeUnit.SECONDS);
        // En esta versión simplificada, no almacenamos el Timeout para cancelarlo posteriormente.
    }
}