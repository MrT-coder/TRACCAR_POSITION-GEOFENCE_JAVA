package com.traccar.PositionGeofence.broadcast;

import java.util.HashSet;
import java.util.Set;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Event;
import com.traccar.PositionGeofence.modelo.ObjectOperation;
import com.traccar.PositionGeofence.modelo.Permission;
import com.traccar.PositionGeofence.modelo.Position;

public abstract class BaseBroadcastService implements BroadcastService {

    private final Set<BroadcastInterface> listeners = new HashSet<>();

    @Override
    public boolean singleInstance() {
        return true;
    }

    @Override
    public void registerListener(BroadcastInterface listener) {
        listeners.add(listener);
    }

    @Override
    public void updateDevice(boolean local, Device device) {
        BroadcastMessage message = new BroadcastMessage();
        message.setDevice(device);
        sendMessage(message);
    }

    @Override
    public void updatePosition(boolean local, Position position) {
        BroadcastMessage message = new BroadcastMessage();
        message.setPosition(position);
        sendMessage(message);
    }

    @Override
    public void updateEvent(boolean local, long userId, Event event) {
        BroadcastMessage message = new BroadcastMessage();
        message.setUserId(userId);
        message.setEvent(event);
        sendMessage(message);
    }

    @Override
    public void updateCommand(boolean local, long deviceId) {
        BroadcastMessage message = new BroadcastMessage();
        message.setCommandDeviceId(deviceId);
        sendMessage(message);
    }

    @Override
    public <T extends BaseModel> void invalidateObject(
            boolean local, Class<T> clazz, long id, ObjectOperation operation) {
        BroadcastMessage message = new BroadcastMessage();
        var invalidateObject = new BroadcastMessage.InvalidateObject();
        invalidateObject.setClazz(Permission.getKey(clazz));
        invalidateObject.setId(id);
        invalidateObject.setOperation(operation);
        message.setInvalidateObject(invalidateObject);
        sendMessage(message);
    }

    @Override
    public synchronized <T1 extends BaseModel, T2 extends BaseModel> void invalidatePermission(
            boolean local, Class<T1> clazz1, long id1, Class<T2> clazz2, long id2, boolean link) {
        BroadcastMessage message = new BroadcastMessage();
        var invalidatePermission = new BroadcastMessage.InvalidatePermission();
        invalidatePermission.setClazz1(Permission.getKey(clazz1));
        invalidatePermission.setId1(id1);
        invalidatePermission.setClazz2(Permission.getKey(clazz2));
        invalidatePermission.setId2(id2);
        invalidatePermission.setLink(link);
        message.setInvalidatePermission(invalidatePermission);
        sendMessage(message);
    }

    protected abstract void sendMessage(BroadcastMessage message);

    protected void handleMessage(BroadcastMessage message) throws Exception {
        if (message.getDevice() != null) {
            listeners.forEach(listener -> listener.updateDevice(false, message.getDevice()));
        } else if (message.getPosition() != null) {
            listeners.forEach(listener -> listener.updatePosition(false, message.getPosition()));
        } else if (message.getUserId() != null && message.getEvent() != null) {
            listeners.forEach(listener -> listener.updateEvent(false, message.getUserId(), message.getEvent()));
        } else if (message.getCommandDeviceId() != null) {
            listeners.forEach(listener -> listener.updateCommand(false, message.getCommandDeviceId()));
        } else if (message.getInvalidateObject() != null) {
            var invalidateObject = message.getInvalidateObject();
            for (BroadcastInterface listener : listeners) {
                listener.invalidateObject(
                        false,
                        Permission.getKeyClass(invalidateObject.getClazz()), invalidateObject.getId(),
                        invalidateObject.getOperation());
            }
        } else if (message.getInvalidatePermission() != null) {
            var invalidatePermission = message.getInvalidatePermission();
            for (BroadcastInterface listener : listeners) {
                listener.invalidatePermission(
                        false,
                        Permission.getKeyClass(invalidatePermission.getClazz1()), invalidatePermission.getId1(),
                        Permission.getKeyClass(invalidatePermission.getClazz2()), invalidatePermission.getId2(),
                        invalidatePermission.getLink());
            }
        }
    }

}