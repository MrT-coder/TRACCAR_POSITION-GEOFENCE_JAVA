package com.traccar.PositionGeofence.handler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.traccar.PositionGeofence.modelo.Event;
import com.traccar.PositionGeofence.modelo.Position;


public abstract class BaseEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEventHandler.class);

    public interface Callback {
        void eventDetected(Event event);
    }

    public void analyzePosition(Position position, Callback callback) {
        try {
            onPosition(position, callback);
        } catch (RuntimeException e) {
            LOGGER.warn("Event handler failed", e);
        }
    }

    /**
     * Event handlers should be processed synchronously.
     */
    public abstract void onPosition(Position position, Callback callback);
}
