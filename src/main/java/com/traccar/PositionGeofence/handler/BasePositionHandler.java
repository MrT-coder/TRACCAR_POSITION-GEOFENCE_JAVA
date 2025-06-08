package com.traccar.PositionGeofence.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.traccar.PositionGeofence.modelo.Position;


public abstract class BasePositionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePositionHandler.class);

    public interface Callback {
        void processed(boolean filtered);
    }

    public abstract void onPosition(Position position, Callback callback);

    public void handlePosition(Position position, Callback callback) {
        try {
            onPosition(position, callback);
        } catch (RuntimeException e) {
            LOGGER.warn("Position handler failed", e);
            callback.processed(false);
        }
    }
}
