package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.User;
import java.util.List;

public interface UserClient {
    List<User> getUsersByDeviceId(Long deviceId);
}