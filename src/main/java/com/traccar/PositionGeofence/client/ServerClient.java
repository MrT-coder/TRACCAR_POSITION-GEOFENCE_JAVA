package com.traccar.PositionGeofence.client;

import java.util.List;

import com.traccar.PositionGeofence.modelo.Server;

public interface ServerClient {
 Server getServer() throws Exception;
}