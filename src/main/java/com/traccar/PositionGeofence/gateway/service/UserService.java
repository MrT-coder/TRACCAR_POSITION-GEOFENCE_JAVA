package com.traccar.PositionGeofence.gateway.service;

import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.client.UserClient;
import com.traccar.PositionGeofence.modelo.User;

import java.util.List;

@Service
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    /**
     * Recupera la lista de usuarios asociados a un dispositivo.
     * La consulta se delega al cliente REST del microservicio de usuarios.
     *
     * @param deviceId Identificador del dispositivo
     * @return Lista de usuarios asociados
     */
    public List<User> getUsersByDeviceId(Long deviceId) {
        return userClient.getUsersByDeviceId(deviceId);
    }
}
