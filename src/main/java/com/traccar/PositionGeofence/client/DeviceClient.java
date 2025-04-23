package com.traccar.PositionGeofence.client;


import java.util.List;

import com.traccar.PositionGeofence.modelo.Device;

public interface DeviceClient {
    //GET /api/devices
    List<Device> getDevices();
    //GET /api/devices/{userId}
    List<Device> getDevicesByUser(long userId) throws Exception;
    //GET /api/devices/{deviceId}
    Device getDeviceById(long deviceId) throws Exception;
    //GET /api/devices/{uniqueId}
    Device getDevicesByUniqueId(String uniqueId) throws Exception;

    //PUT /api/devices/{deviceId}
    Device updateDevice(Device device);
}
