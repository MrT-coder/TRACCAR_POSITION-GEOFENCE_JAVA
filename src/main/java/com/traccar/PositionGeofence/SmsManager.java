package com.traccar.PositionGeofence;

import org.springframework.messaging.MessagingException;

public interface SmsManager {
    void sendMessage(String phone, String message, boolean command) throws MessagingException;
}
