package com.traccar.PositionGeofence.gateway.controller;

import com.traccar.PositionGeofence.gateway.service.ServerService;
import com.traccar.PositionGeofence.modelo.Server;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server")
public class ServerGatewayController {

    private final ServerService serverService;

    public ServerGatewayController(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Endpoint para obtener la información global del servidor.
     */
    @GetMapping
    public ResponseEntity<Server> getServer() {
        try {
            // Se podría definir un ID fijo (por ejemplo, 1) o implementar otra lógica para seleccionar el servidor global.
            Server server = serverService.getServerById(1L);
            if (server != null) {
                return ResponseEntity.ok(server);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}