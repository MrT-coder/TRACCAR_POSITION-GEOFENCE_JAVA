package com.traccar.PositionGeofence.gateway.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.client.DeviceClient;
import com.traccar.PositionGeofence.modelo.Device;

@Service
public class DeviceService {

    private final DeviceClient deviceClient;

    public DeviceService(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

   public List<Device> getDevices() {
        return deviceClient.getDevices();
    }

    public Device updateDevice(Device device) {
        return deviceClient.updateDevice(device);
    }

    public List<Device> getDevicesByUser(long userId) throws Exception {
        return deviceClient.getDevicesByUser(userId);
    }
    public Device getDeviceById(long deviceId) throws Exception {
        return deviceClient.getDeviceById(deviceId);
    }
    public Device getDevicesByUniqueId(String uniqueId) throws Exception {
        return deviceClient.getDevicesByUniqueId(uniqueId);
    }

}
