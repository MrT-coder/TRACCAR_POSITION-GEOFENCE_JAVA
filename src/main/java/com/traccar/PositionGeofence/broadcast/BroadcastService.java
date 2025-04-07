package com.traccar.PositionGeofence.broadcast;

import com.traccar.PositionGeofence.LifecycleObject;

public interface BroadcastService extends LifecycleObject, BroadcastInterface {
    boolean singleInstance();
    void registerListener(BroadcastInterface listener);
}

