package com.traccar.PositionGeofence.gateway.service;


import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.client.DeviceClient;
import com.traccar.PositionGeofence.modelo.Device;

@Service
public class DeviceService {

    private final DeviceClient deviceClient;

    public DeviceService(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    /**
     * Recupera la información del dispositivo por su ID.
     * Esto delega la consulta a través del cliente REST.
     *
     * @param deviceId Identificador del dispositivo
     * @return Objeto Device o null si no se encuentra
     */
    public Device getDeviceById(String deviceId) {
        return deviceClient.getDeviceById(deviceId);
    }
}
