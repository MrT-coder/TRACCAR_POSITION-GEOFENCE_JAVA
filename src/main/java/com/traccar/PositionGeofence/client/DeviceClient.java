package com.traccar.PositionGeofence.client;


import com.traccar.PositionGeofence.modelo.Device;

public interface DeviceClient {
    Device getDeviceById(String uniqueId);
}
