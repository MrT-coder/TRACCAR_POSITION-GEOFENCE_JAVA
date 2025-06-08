package com.traccar.PositionGeofence.client;


import com.traccar.PositionGeofence.modelo.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Component
public class ServerClientImpl implements ServerClient {

    private final RestTemplate restTemplate;
    private final String serverServiceBaseUrl;


    public ServerClientImpl(@Value("${gateway.server.baseUrl}") String serverServiceBaseUrl) {
        this.serverServiceBaseUrl = serverServiceBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Server getServer() throws Exception {
        String url = serverServiceBaseUrl + "/server"; // Construimos el endpoint
        return restTemplate.getForObject(url, Server.class);
    }
}