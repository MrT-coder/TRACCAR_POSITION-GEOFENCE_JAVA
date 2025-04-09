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

    /**
     * Obtiene la información del Server dado su ID.
     * Este método usa el ServerClient para invocar el endpoint correspondiente del API Gateway (o el microservicio de User)
     * que gestiona la información del servidor.
     *
     * @param serverId Identificador del servidor
     * @return El objeto Server, o null si no se encontró
     * @throws Exception Si ocurre algún error durante la consulta
     */
    public Server getServerById(Long serverId) throws Exception {
        List<Server> servers = serverClient.getServersById(serverId);
        if (servers != null && !servers.isEmpty()) {
            LOGGER.debug("Server found for id {}: {}", serverId, servers.get(0));
            return servers.get(0);
        } else {
            LOGGER.warn("No server found for id {}", serverId);
            return null;
        }
    }
}
