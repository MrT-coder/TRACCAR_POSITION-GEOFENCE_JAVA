package com.traccar.PositionGeofence.client;


import java.util.List;

import com.traccar.PositionGeofence.modelo.Device;

public interface DeviceClient {
    Device getDeviceById(String uniqueId);
    Device updateDevice(Device device);
    List<Device> getDevicesForUser(long userId) throws Exception;
}
