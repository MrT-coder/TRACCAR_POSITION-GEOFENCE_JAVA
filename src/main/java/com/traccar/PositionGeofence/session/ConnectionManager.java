package com.traccar.PositionGeofence.session;

import io.netty.channel.Channel;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.broadcast.BroadcastInterface;
import com.traccar.PositionGeofence.broadcast.BroadcastService;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Event;
import com.traccar.PositionGeofence.modelo.LogRecord;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.session.cache.CacheManager;
import com.traccar.PositionGeofence.storage.Storage;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ConnectionManager implements BroadcastInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    // Tiempo de inactividad (en segundos) antes de marcar un dispositivo inactivo
    private final long deviceTimeout;
    private final boolean showUnknownDevices;

    private final Map<Long, DeviceSession> sessionsByDeviceId = new ConcurrentHashMap<>();
    private final Map<ConnectionKey, Map<String, DeviceSession>> sessionsByEndpoint = new ConcurrentHashMap<>();
    private final Map<ConnectionKey, String> unknownByEndpoint = new ConcurrentHashMap<>();

    private final CacheManager cacheManager;
    private final Timer timer;
    private final BroadcastService broadcastService;

    // Clientes REST para obtener dispositivos y servidores (delegados a API
    // Gateway)
    private final com.traccar.PositionGeofence.client.DeviceClient deviceClient;
    // En este microservicio es posible que la lógica para usuarios y notificaciones
    // se use en el CacheManager, pero aquí se delega al gateway
    // private final DeviceLookupService deviceLookupService; // Ya no se usa

    private final Map<Long, Set<UpdateListener>> listeners = new HashMap<>();
    // Las relaciones entre usuarios y dispositivos se mantienen para broadcast
    private final Map<Long, Set<Long>> userDevices = new HashMap<>();
    private final Map<Long, Set<Long>> deviceUsers = new HashMap<>();

    private final Map<Long, Timeout> timeouts = new ConcurrentHashMap<>();

    public ConnectionManager(
            Config config, CacheManager cacheManager, Storage storage,
            Timer timer, BroadcastService broadcastService,
            com.traccar.PositionGeofence.client.DeviceClient deviceClient) {
        this.cacheManager = cacheManager;
        this.timer = timer;
        this.broadcastService = broadcastService;
        this.deviceClient = deviceClient;
        deviceTimeout = config.getLong(Keys.STATUS_TIMEOUT); 
        showUnknownDevices = config.getBoolean(Keys.WEB_SHOW_UNKNOWN_DEVICES);
        broadcastService.registerListener(this);
    }

    public DeviceSession getDeviceSession(long deviceId) {
        return sessionsByDeviceId.get(deviceId);
    }

    public DeviceSession getDeviceSession(Protocol protocol, Channel channel, SocketAddress remoteAddress,
            String... uniqueIds)
            throws Exception {
        ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
        Map<String, DeviceSession> endpointSessions = sessionsByEndpoint.getOrDefault(connectionKey,
                new ConcurrentHashMap<>());

        uniqueIds = Arrays.stream(uniqueIds).filter(Objects::nonNull).toArray(String[]::new);
        if (uniqueIds.length > 0) {
            for (String uniqueId : uniqueIds) {
                DeviceSession deviceSession = endpointSessions.get(uniqueId);
                if (deviceSession != null) {
                    return deviceSession;
                }
            }
        } else {
            return endpointSessions.values().stream().findAny().orElse(null);
        }

        // Se obtiene el dispositivo vía REST
        Device device = deviceClient.getDevicesByUniqueId(uniqueIds[0]);
        String firstUniqueId = uniqueIds[0];
        if (device == null) {
            LOGGER.warn("Unknown device - " + String.join(" ", uniqueIds)
                    + " (" + ((InetSocketAddress) remoteAddress).getHostString() + ")");
            unknownByEndpoint.put(connectionKey, firstUniqueId);
            return null;
        }

        unknownByEndpoint.remove(connectionKey);
        device.checkDisabled();

        DeviceSession oldSession = sessionsByDeviceId.remove(device.getId());
        if (oldSession != null) {
            Map<String, DeviceSession> oldEndpointSessions = sessionsByEndpoint.get(oldSession.getConnectionKey());
            if (oldEndpointSessions != null && oldEndpointSessions.size() > 1) {
                oldEndpointSessions.remove(device.getUniqueId());
            } else {
                sessionsByEndpoint.remove(oldSession.getConnectionKey());
            }
        }

        DeviceSession deviceSession = new DeviceSession(
                device.getId(), device.getUniqueId(), device.getModel(), protocol, channel, remoteAddress);
        endpointSessions.put(device.getUniqueId(), deviceSession);
        sessionsByEndpoint.put(connectionKey, endpointSessions);
        sessionsByDeviceId.put(device.getId(), deviceSession);

        if (oldSession == null) {
            cacheManager.addDevice(device.getId(), connectionKey);
        }

        return deviceSession;
    }

    public void deviceDisconnected(Channel channel, boolean supportsOffline) throws Exception {
        SocketAddress remoteAddress = channel.remoteAddress();
        if (remoteAddress != null) {
            ConnectionKey connectionKey = new ConnectionKey(channel, remoteAddress);
            Map<String, DeviceSession> endpointSessions = sessionsByEndpoint.remove(connectionKey);
            if (endpointSessions != null) {
                for (DeviceSession deviceSession : endpointSessions.values()) {
                    if (supportsOffline) {
                        updateDevice(deviceSession.getDeviceId(), Device.STATUS_OFFLINE, null);
                    }
                    sessionsByDeviceId.remove(deviceSession.getDeviceId());
                    cacheManager.removeDevice(deviceSession.getDeviceId(), connectionKey);
                }
            }
            unknownByEndpoint.remove(connectionKey);
        }
    }

    public void deviceUnknown(long deviceId) throws Exception {
        updateDevice(deviceId, Device.STATUS_UNKNOWN, null);
        removeDeviceSession(deviceId);
    }

    private void removeDeviceSession(long deviceId) {
        DeviceSession deviceSession = sessionsByDeviceId.remove(deviceId);
        if (deviceSession != null) {
            ConnectionKey connectionKey = deviceSession.getConnectionKey();
            cacheManager.removeDevice(deviceId, connectionKey);
            sessionsByEndpoint.computeIfPresent(connectionKey, (key, sessions) -> {
                sessions.remove(deviceSession.getUniqueId());
                return sessions.isEmpty() ? null : sessions;
            });
        }
    }

    /**
     * Actualiza el estado de un dispositivo:
     * - Obtiene el dispositivo (vía CacheManager o mediante el cliente REST, si es
     * necesario).
     * - Actualiza el estado y la última actualización.
     * - Propaga el cambio mediante BroadcastService.
     * - Actualiza timeouts de inactividad.
     * @throws Exception 
     */
    public void updateDevice(long deviceId, String status, Date time) throws Exception {
        Device device = deviceClient.getDeviceById(deviceId);
        if (device == null) {
            LOGGER.warn("Device not found for id: {}", deviceId);
            return;
        }

        String oldStatus = device.getStatus();
        device.setStatus(status);
        if (time != null) {
            device.setLastUpdate(time);
        }

        if (!status.equals(oldStatus)) {
            String eventType;
            Map<Event, Position> events = new HashMap<>();
            eventType = switch (status) {
                case Device.STATUS_ONLINE -> Event.TYPE_DEVICE_ONLINE;
                case Device.STATUS_UNKNOWN -> Event.TYPE_DEVICE_UNKNOWN;
                default -> Event.TYPE_DEVICE_OFFLINE;
            };
            events.put(new Event(eventType, deviceId), null);
            // Aquí se invoca el NotificationManager (o su equivalente) para propagar
            // eventos.
            // Se asume que notificationManager.updateEvents(events) funciona; si no, delega
            // a un servicio REST.
        }

        Timeout timeout = timeouts.remove(deviceId);
        if (timeout != null) {
            timeout.cancel();
        }

        if (status.equals(Device.STATUS_ONLINE)) {
            timeouts.put(deviceId, timer.newTimeout(t -> {
                if (!t.isCancelled()) {
                    deviceUnknown(deviceId);
                }
            }, deviceTimeout, TimeUnit.SECONDS));
        }

        // **Delegamos la actualización del dispositivo mediante REST**
    try {
        deviceClient.updateDevice(device);
    } catch (Exception e) {
        LOGGER.warn("Failed to update device through REST", e);
    }

    updateDevice(true, device);
    }

    public synchronized void sendKeepalive() {
        for (Set<UpdateListener> userListeners : listeners.values()) {
            for (UpdateListener listener : userListeners) {
                listener.onKeepalive();
            }
        }
    }

    @Override
    public synchronized void updateDevice(boolean local, Device device) {
        if (local) {
            broadcastService.updateDevice(true, device);
        } else if (Device.STATUS_ONLINE.equals(device.getStatus())) {
            timeouts.remove(device.getId());
            removeDeviceSession(device.getId());
        }
        for (long userId : deviceUsers.getOrDefault(device.getId(), Collections.emptySet())) {
            if (listeners.containsKey(userId)) {
                for (UpdateListener listener : listeners.get(userId)) {
                    listener.onUpdateDevice(device);
                }
            }
        }
    }

    @Override
    public synchronized void updatePosition(boolean local, Position position) {
        if (local) {
            broadcastService.updatePosition(true, position);
        }
        for (long userId : deviceUsers.getOrDefault(position.getDeviceId(), Collections.emptySet())) {
            if (listeners.containsKey(userId)) {
                for (UpdateListener listener : listeners.get(userId)) {
                    listener.onUpdatePosition(position);
                }
            }
        }
    }

    @Override
    public synchronized void updateEvent(boolean local, long userId, Event event) {
        if (local) {
            broadcastService.updateEvent(true, userId, event);
        }
        if (listeners.containsKey(userId)) {
            for (UpdateListener listener : listeners.get(userId)) {
                listener.onUpdateEvent(event);
            }
        }
    }

    @Override
    public synchronized <T1 extends BaseModel, T2 extends BaseModel> void invalidatePermission(
            boolean local, Class<T1> clazz1, long id1, Class<T2> clazz2, long id2, boolean link) throws Exception {
        if (local) {
            broadcastService.invalidatePermission(true, clazz1, id1, clazz2, id2, link);
        }
        // Este método se usa únicamente para actualizaciones internas de caché;
        // si la lógica de permisos se gestiona en otro microservicio, aquí podrías
        // dejar una implementación básica o comentarla.
    }

    public synchronized void updateLog(LogRecord record) {
        var sessions = sessionsByEndpoint.getOrDefault(record.getConnectionKey(), Map.of());
        if (sessions.isEmpty()) {
            String unknownUniqueId = unknownByEndpoint.get(record.getConnectionKey());
            if (unknownUniqueId != null && showUnknownDevices) {
                record.setUniqueId(unknownUniqueId);
                listeners.values().stream()
                        .flatMap(Set::stream)
                        .forEach(listener -> listener.onUpdateLog(record));
            }
        } else {
            var firstEntry = sessions.entrySet().iterator().next();
            record.setUniqueId(firstEntry.getKey());
            record.setDeviceId(firstEntry.getValue().getDeviceId());
            for (long userId : deviceUsers.getOrDefault(record.getDeviceId(), Set.of())) {
                for (UpdateListener listener : listeners.getOrDefault(userId, Set.of())) {
                    listener.onUpdateLog(record);
                }
            }
        }
    }

    public interface UpdateListener {
        void onKeepalive();

        void onUpdateDevice(Device device);

        void onUpdatePosition(Position position);

        void onUpdateEvent(Event event);

        void onUpdateLog(LogRecord record);
    }

    // public synchronized void addListener(long userId, UpdateListener listener) throws StorageException {
    //     var set = listeners.get(userId);
    //     if (set == null) {
    //         set = new HashSet<>();
    //         listeners.put(userId, set);

    //         // Si la lógica de permisos no está implementada en este microservicio, se puede
    //         // omitir esta parte,
    //         // o delegar la consulta a un cliente REST de permisos.
    //         var devices = storage.getObjects(Device.class,
    //                 new Request(new Columns.Include("id"), new Condition.Permission(User.class, userId, Device.class)));
    //         userDevices.put(userId, devices.stream().map(BaseModel::getId).collect(Collectors.toSet()));
    //         devices.forEach(device -> deviceUsers.computeIfAbsent(device.getId(), id -> new HashSet<>()).add(userId));
    //     }
    //     set.add(listener);
    // }

    public synchronized void removeListener(long userId, UpdateListener listener) {
        var set = listeners.get(userId);
        set.remove(listener);
        if (set.isEmpty()) {
            listeners.remove(userId);
            userDevices.remove(userId).forEach(deviceId -> deviceUsers.computeIfPresent(deviceId, (x, userIds) -> {
                userIds.remove(userId);
                return userIds.isEmpty() ? null : userIds;
            }));
        }
    }
}