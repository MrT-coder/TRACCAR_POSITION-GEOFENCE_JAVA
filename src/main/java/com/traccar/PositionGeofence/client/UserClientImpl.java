package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Component
public class UserClientImpl implements UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserClientImpl(@Value("${gateway.user.baseUrl}") String userServiceBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public List<User> getUsersByDeviceId(Long deviceId) {
        String url = userServiceBaseUrl + "/devices/" + deviceId + "/users";
        User[] users = restTemplate.getForObject(url, User[].class);
        return users != null ? Arrays.asList(users) : List.of();
    }
}