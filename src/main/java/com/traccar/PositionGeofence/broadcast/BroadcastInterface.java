package com.traccar.PositionGeofence.broadcast;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Event;
import com.traccar.PositionGeofence.modelo.ObjectOperation;
import com.traccar.PositionGeofence.modelo.Position;

public interface BroadcastInterface {

    default void updateDevice(boolean local, Device device) {
    }

    default void updatePosition(boolean local, Position position) {
    }

    default void updateEvent(boolean local, long userId, Event event) {
    }

    default void updateCommand(boolean local, long deviceId) {
    }

    default <T extends BaseModel> void invalidateObject(
            boolean local, Class<T> clazz, long id, ObjectOperation operation) throws Exception {
    }

    default <T1 extends BaseModel, T2 extends BaseModel> void invalidatePermission(
            boolean local, Class<T1> clazz1, long id1, Class<T2> clazz2, long id2, boolean link) throws Exception {
    }

}