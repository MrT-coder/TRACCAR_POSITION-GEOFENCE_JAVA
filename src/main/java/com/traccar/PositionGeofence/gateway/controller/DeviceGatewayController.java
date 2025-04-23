package com.traccar.PositionGeofence.gateway.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.traccar.PositionGeofence.gateway.service.DeviceService;
import com.traccar.PositionGeofence.gateway.service.NotificationService;
import com.traccar.PositionGeofence.gateway.service.UserService;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Notification;
import com.traccar.PositionGeofence.modelo.User;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceGatewayController {

    private final DeviceService deviceService;
    private final UserService userService;
    private final NotificationService notificationService;

    public DeviceGatewayController(DeviceService deviceService,
                                   UserService userService,
                                   NotificationService notificationService) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Endpoint para obtener todos los dispositivos.
     */
    @GetMapping
    public ResponseEntity<List<Device>> getDevices() {
        List<Device> devices = deviceService.getDevices();
        return ResponseEntity.ok(devices);
    }  

    /**
     * Endpoint para actualizar un dispositivo.
     */
    @PutMapping("/{deviceId}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long deviceId, @RequestBody Device device) {
        device.setId(deviceId); // Asegúrate de que el ID del dispositivo en el cuerpo de la solicitud sea correcto
        Device updatedDevice = deviceService.updateDevice(device);
        return ResponseEntity.ok(updatedDevice);
    }

    /**
     * Endpoint para obtener los dispositivos de un usuario específico.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Device>> getDevicesByUser(@PathVariable Long userId) throws Exception {
        List<Device> devices = deviceService.getDevicesByUser(userId);
        return ResponseEntity.ok(devices);
    }

    /**
     * Endpoint para obtener un dispositivo por su Unique ID.
     */
    @GetMapping("/{uniqueId}")
    public ResponseEntity<Device> getDevicesByUniqueId(@PathVariable String uniqueId) throws Exception {
        Device devices = deviceService.getDevicesByUniqueId(uniqueId);
        return ResponseEntity.ok(devices);
    }

    /**
     Endpoint para obtener los dispositivos de un deviceid específico.
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long deviceId) throws Exception {
        Device device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(device);
    }
}
