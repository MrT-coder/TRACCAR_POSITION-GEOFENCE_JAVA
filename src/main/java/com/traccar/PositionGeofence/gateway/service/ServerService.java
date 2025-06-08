package com.traccar.PositionGeofence.gateway.service;


import com.traccar.PositionGeofence.client.ServerClient;
import com.traccar.PositionGeofence.modelo.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);
    
    private final ServerClient serverClient;

    public ServerService(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public Server getServer() throws Exception {
        LOGGER.info("Fetching server information from the server service.");
        return serverClient.getServer();
    }
}
