package com.traccar.PositionGeofence.client;

import java.util.List;

import com.traccar.PositionGeofence.modelo.Server;

public interface ServerClient {

 List<Server> getServersById(Long ServerId) throws Exception;
 Server getServer() throws Exception;
}