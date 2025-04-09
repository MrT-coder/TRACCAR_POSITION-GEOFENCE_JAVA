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
     * Endpoint para obtener la información de un dispositivo dado su id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDevice(@PathVariable("id") String id) {
        Device device = deviceService.getDeviceById(id);
        if (device != null) {
            return ResponseEntity.ok(device);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para obtener los usuarios asociados a un dispositivo.
     */
    @GetMapping("/{id}/users")
    public ResponseEntity<List<User>> getDeviceUsers(@PathVariable("id") Long deviceId) {
        List<User> users = userService.getUsersByDeviceId(deviceId);
        return ResponseEntity.ok(users);
    }

    /**
     * Endpoint para obtener las notificaciones configuradas para el dispositivo.
     */
    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<Notification>> getDeviceNotifications(@PathVariable("id") Long deviceId) {
        List<Notification> notifications = notificationService.getDeviceNotifications(deviceId);
        return ResponseEntity.ok(notifications);
    }
}
