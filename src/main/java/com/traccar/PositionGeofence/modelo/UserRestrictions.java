package com.traccar.PositionGeofence.modelo;

public interface UserRestrictions {
    boolean getReadonly();
    boolean getDeviceReadonly();
    boolean getLimitCommands();
    boolean getDisableReports();
    boolean getFixedEmail();
}