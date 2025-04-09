package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Component
public class NotificationClientImpl implements NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceBaseUrl;

    public NotificationClientImpl(@Value("${gateway.notification.baseUrl}") String notificationServiceBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    @Override
    public List<Notification> getDeviceNotifications(Long deviceId) {
        String url = notificationServiceBaseUrl + "/devices/" + deviceId + "/notifications";
        Notification[] notifications = restTemplate.getForObject(url, Notification[].class);
        return notifications != null ? Arrays.asList(notifications) : List.of();
    }
}