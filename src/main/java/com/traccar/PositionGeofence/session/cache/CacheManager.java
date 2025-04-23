package com.traccar.PositionGeofence.session.cache;

import com.traccar.PositionGeofence.client.DeviceClient;
import com.traccar.PositionGeofence.client.NotificationClient;
import com.traccar.PositionGeofence.client.ServerClient;
import com.traccar.PositionGeofence.client.UserClient;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.modelo.Attribute;
import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Calendar;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Driver;
import com.traccar.PositionGeofence.modelo.Geofence;
import com.traccar.PositionGeofence.modelo.Group;
import com.traccar.PositionGeofence.modelo.GroupedModel;
import com.traccar.PositionGeofence.modelo.Maintenance;
import com.traccar.PositionGeofence.modelo.Notification;
import com.traccar.PositionGeofence.modelo.ObjectOperation;
import com.traccar.PositionGeofence.modelo.Permission;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.modelo.Schedulable;
import com.traccar.PositionGeofence.modelo.Server;
import com.traccar.PositionGeofence.modelo.User;
import com.traccar.PositionGeofence.broadcast.BroadcastInterface;
import com.traccar.PositionGeofence.broadcast.BroadcastService;
import com.traccar.PositionGeofence.storage.Storage;
import com.traccar.PositionGeofence.storage.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
public class CacheManager implements BroadcastInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);

    private static final Set<Class<? extends BaseModel>> GROUPED_CLASSES =
            Set.of(Attribute.class, Driver.class, Geofence.class, Maintenance.class, Notification.class);

    private final Config config;
    private final Storage storage;
    private final BroadcastService broadcastService;
    
    // Clientes REST para obtener información de otros microservicios
    private final DeviceClient deviceClient;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final ServerClient serverClient;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final CacheGraph graph = new CacheGraph();

    // Las posiciones se gestionan localmente
    private final Map<Long, Position> devicePositions = new HashMap<>();
    private final Map<Long, HashSet<Object>> deviceReferences = new HashMap<>();

    // La información de Server se obtiene mediante REST
    private Server server;

    public CacheManager(Config config, Storage storage, BroadcastService broadcastService,
                        DeviceClient deviceClient, UserClient userClient,
                        NotificationClient notificationClient, ServerClient serverClient)
            throws Exception {
        this.config = config;
        this.storage = storage;
        this.broadcastService = broadcastService;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.notificationClient = notificationClient;
        this.serverClient = serverClient;
        this.server = serverClient.getServer();
        broadcastService.registerListener(this);
    }

    @Override
    public String toString() {
        return graph.toString();
    }

    public Config getConfig() {
        return config;
    }

    public <T extends BaseModel> T getObject(Class<T> clazz, long id) {
        lock.readLock().lock();
        try {
            return graph.getObject(clazz, id);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T extends BaseModel> Set<T> getDeviceObjects(long deviceId, Class<T> clazz) {
        lock.readLock().lock();
        try {
            return graph.getObjects(Device.class, deviceId, clazz, Set.of(Group.class), true)
                        .collect(Collectors.toUnmodifiableSet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retorna la última posición registrada para un dispositivo.
     */
    public Position getPosition(long deviceId) {
        lock.readLock().lock();
        try {
            return devicePositions.get(deviceId);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retorna la información global del servidor a través del ServerClient.
     * @throws Exception 
     */
    public Server getServer() throws Exception {
        lock.readLock().lock();
        try {
            if (server == null) {
                server = serverClient.getServer();
            }
            return server;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Obtiene los usuarios para notificaciones asociados a un dispositivo.
     * Si la información en cache es insuficiente, se consulta mediante el UserClient.
     */
    public Set<User> getNotificationUsers(long notificationId, long deviceId) {
        lock.readLock().lock();
        try {
            Set<User> cachedUsers = graph.getObjects(Notification.class, notificationId, User.class, Set.of(), false)
                    .collect(Collectors.toUnmodifiableSet());
            if (cachedUsers.isEmpty()) {
                return new HashSet<>(userClient.getUsersByDeviceId(Long.valueOf(deviceId)));
            }
            return cachedUsers;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Obtiene las notificaciones configuradas para un dispositivo.
     * Si no hay datos en cache, se consulta mediante el NotificationClient.
     */
    public Set<Notification> getDeviceNotifications(long deviceId) {
        lock.readLock().lock();
        try {
            var direct = graph.getObjects(Device.class, deviceId, Notification.class, Set.of(Group.class), true)
                    .map(BaseModel::getId)
                    .collect(Collectors.toUnmodifiableSet());
            Set<Notification> cachedNotifs = graph.getObjects(Device.class, deviceId, Notification.class, Set.of(Group.class, User.class), true)
                    .filter(notification -> notification.getAlways() || direct.contains(notification.getId()))
                    .collect(Collectors.toUnmodifiableSet());
            if (cachedNotifs.isEmpty()) {
                return new HashSet<>(notificationClient.getDeviceNotifications(Long.valueOf(deviceId)));
            }
            return cachedNotifs;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Registra un dispositivo en la caché.
     * Se consulta al DeviceClient para obtener la información del dispositivo.
     * La posición se consulta a través del Storage (usando QueryRequest) ya que esta es responsabilidad del microservicio.
     */
    public void addDevice(long deviceId, Object key) throws Exception {
        lock.writeLock().lock();
        try {
            var references = deviceReferences.computeIfAbsent(deviceId, k -> new HashSet<>());
            if (references.isEmpty()) {
                // Consultar el Device a través del cliente REST
                Device device = deviceClient.getDeviceById(deviceId);
                if (device == null) {
                    throw new Exception("Device not found for id " + deviceId);
                }
                graph.addObject(device);
                initializeCache(device);
                if (device.getPositionId() > 0) {
                    // Utiliza QueryRequest con Spring Data MongoDB para obtener la posición
                    Query query = new Query();
                    query.addCriteria(Criteria.where("id").is(device.getPositionId()));
                    QueryRequest queryRequest = new QueryRequest(query);
                    List<Position> positions = storage.getObjects(Position.class, queryRequest);
                    if (!positions.isEmpty()) {
                        devicePositions.put(deviceId, positions.get(0));
                    }
                }
            }
            references.add(key);
            LOGGER.debug("Cache add device {} references {} key {}", deviceId, references.size(), key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Elimina un dispositivo de la caché. Si no quedan referencias, se elimina completamente.
     */
    public void removeDevice(long deviceId, Object key) {
        lock.writeLock().lock();
        try {
            var references = deviceReferences.computeIfAbsent(deviceId, k -> new HashSet<>());
            references.remove(key);
            if (references.isEmpty()) {
                graph.removeObject(Device.class, deviceId);
                devicePositions.remove(deviceId);
                deviceReferences.remove(deviceId);
            }
            LOGGER.debug("Cache remove device {} references {} key {}", deviceId, references.size(), key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Actualiza la posición de un dispositivo en la caché.
     */
    public void updatePosition(Position position) {
        lock.writeLock().lock();
        try {
            if (deviceReferences.containsKey(position.getDeviceId())) {
                devicePositions.put(position.getDeviceId(), position);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
public <T extends BaseModel> void invalidateObject(boolean local, Class<T> clazz, long id, ObjectOperation operation) throws Exception {
    if (local) {
        broadcastService.invalidateObject(true, clazz, id, operation);
    }
    if (operation == ObjectOperation.DELETE) {
        graph.removeObject(clazz, id);
        return;
    }
    if (operation != ObjectOperation.UPDATE) {
        return;
    }
    if (clazz.equals(Server.class)) {
        server = serverClient.getServer();
        if (server == null) {
            LOGGER.error("Server not found for id {}", id);
            return;
        }
        return;
    }
    // Crear una consulta MongoDB para buscar el objeto por "id"
    Query query = new Query();
    query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("id").is(id));
    QueryRequest queryRequest = new QueryRequest(query);
    T after = storage.getObject(clazz, queryRequest);
    if (after == null) {
        return;
    }
    T before = (T) getObject(after.getClass(), after.getId());
    if (before == null) {
        return;
    }
    if (after instanceof GroupedModel) {
        long beforeGroupId = ((GroupedModel) before).getGroupId();
        long afterGroupId = ((GroupedModel) after).getGroupId();
        if (beforeGroupId != afterGroupId) {
            if (beforeGroupId > 0) {
                invalidatePermission(clazz, id, Group.class, beforeGroupId, false);
            }
            if (afterGroupId > 0) {
                invalidatePermission(clazz, id, Group.class, afterGroupId, true);
            }
        }
    } else if (after instanceof Schedulable) {
        long beforeCalendarId = ((Schedulable) before).getCalendarId();
        long afterCalendarId = ((Schedulable) after).getCalendarId();
        if (beforeCalendarId != afterCalendarId) {
            if (beforeCalendarId > 0) {
                invalidatePermission(clazz, id, Calendar.class, beforeCalendarId, false);
            }
            if (afterCalendarId > 0) {
                invalidatePermission(clazz, id, Calendar.class, afterCalendarId, true);
            }
        }
    }
    graph.updateObject(after);
}

@Override
public <T1 extends BaseModel, T2 extends BaseModel> void invalidatePermission(
        boolean local, Class<T1> clazz1, long id1, Class<T2> clazz2, long id2, boolean link) throws Exception {
    if (local) {
        broadcastService.invalidatePermission(true, clazz1, id1, clazz2, id2, link);
    }
    if (clazz1.equals(User.class) && GroupedModel.class.isAssignableFrom(clazz2)) {
        invalidatePermission(clazz2, id2, clazz1, id1, link);
    } else {
        invalidatePermission(clazz1, id1, clazz2, id2, link);
    }
}

private <T1 extends BaseModel, T2 extends BaseModel> void invalidatePermission(
        Class<T1> fromClass, long fromId, Class<T2> toClass, long toId, boolean link) throws Exception {
    boolean groupLink = GroupedModel.class.isAssignableFrom(fromClass) && toClass.equals(Group.class);
    boolean calendarLink = Schedulable.class.isAssignableFrom(fromClass) && toClass.equals(Calendar.class);
    boolean userLink = fromClass.equals(User.class) && toClass.equals(Notification.class);
    boolean groupedLinks = GroupedModel.class.isAssignableFrom(fromClass)
            && (GROUPED_CLASSES.contains(toClass) || toClass.equals(User.class));
    if (!groupLink && !calendarLink && !userLink && !groupedLinks) {
        return;
    }
    if (link) {
        // Utilizamos QueryRequest para obtener el objeto destino por su id.
        Query query = new Query();
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("id").is(toId));
        QueryRequest queryRequest = new QueryRequest(query);
        BaseModel object = storage.getObject(toClass, queryRequest);
        if (!graph.addLink(fromClass, fromId, object)) {
            initializeCache(object);
        }
    } else {
        graph.removeLink(fromClass, fromId, toClass, toId);
    }
}

private void initializeCache(BaseModel object) throws Exception {
    if (object instanceof User) {
        for (Permission permission : storage.getPermissions(User.class, Notification.class)) {
            if (permission.getOwnerId() == object.getId()) {
                invalidatePermission(
                        permission.getOwnerClass(), permission.getOwnerId(),
                        permission.getPropertyClass(), permission.getPropertyId(), true);
            }
        }
    } else {
        if (object instanceof GroupedModel groupedModel) {
            long groupId = groupedModel.getGroupId();
            if (groupId > 0) {
                invalidatePermission(object.getClass(), object.getId(), Group.class, groupId, true);
            }
            for (Permission permission : storage.getPermissions(User.class, object.getClass())) {
                if (permission.getPropertyId() == object.getId()) {
                    invalidatePermission(
                            object.getClass(), object.getId(), User.class, permission.getOwnerId(), true);
                }
            }
            for (Class<? extends BaseModel> clazz : GROUPED_CLASSES) {
                for (Permission permission : storage.getPermissions(object.getClass(), clazz)) {
                    if (permission.getOwnerId() == object.getId()) {
                        invalidatePermission(
                                object.getClass(), object.getId(), clazz, permission.getPropertyId(), true);
                    }
                }
            }
        }
        if (object instanceof Schedulable schedulable) {
            long calendarId = schedulable.getCalendarId();
            if (calendarId > 0) {
                invalidatePermission(object.getClass(), object.getId(), Calendar.class, calendarId, true);
            }
        }
    }
}
}