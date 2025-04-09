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
    public List<Server> getServersById(Long serverId) throws Exception {
        // Construimos el endpoint, por ejemplo: http://<baseUrl>/servers/{id}
        String url = serverServiceBaseUrl + "/servers/" + serverId;
        Server[] servers = restTemplate.getForObject(url, Server[].class);
        return (servers != null) ? Arrays.asList(servers) : List.of();
    }
}