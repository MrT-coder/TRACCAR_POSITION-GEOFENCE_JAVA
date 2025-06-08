package com.traccar.PositionGeofence.broadcast;

import org.springframework.stereotype.Component;
import com.traccar.PositionGeofence.LifecycleObject;

@Component
public interface BroadcastService extends LifecycleObject, BroadcastInterface {
    boolean singleInstance();
    void registerListener(BroadcastInterface listener);
}

