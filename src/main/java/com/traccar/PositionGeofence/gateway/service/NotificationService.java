package com.traccar.PositionGeofence.gateway.service;


import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.client.NotificationClient;
import com.traccar.PositionGeofence.modelo.Notification;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationClient notificationClient;

    public NotificationService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    /**
     * Recupera las notificaciones configuradas para un dispositivo.
     * La lógica se delega al cliente REST que consulta el endpoint adecuado.
     *
     * @param deviceId Identificador del dispositivo
     * @return Lista de notificaciones
     */
    public List<Notification> getDeviceNotifications(Long deviceId) {
        return notificationClient.getDeviceNotifications(deviceId);
    }
}
