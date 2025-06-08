package com.traccar.PositionGeofence.modelo;

import java.util.Date;

public interface Disableable {

    boolean getDisabled();

    void setDisabled(boolean disabled);

    Date getExpirationTime();

    void setExpirationTime(Date expirationTime);

    default void checkDisabled() throws SecurityException {
        if (getDisabled()) {
            throw new SecurityException(getClass().getSimpleName() + " is disabled");
        }
        if (getExpirationTime() != null && System.currentTimeMillis() > getExpirationTime().getTime()) {
            throw new SecurityException(getClass().getSimpleName() + " has expired");
        }
    }

}
