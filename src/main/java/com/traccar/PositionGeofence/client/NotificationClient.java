package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.Notification;
import java.util.List;

public interface NotificationClient {
    List<Notification> getDeviceNotifications(Long deviceId);
}