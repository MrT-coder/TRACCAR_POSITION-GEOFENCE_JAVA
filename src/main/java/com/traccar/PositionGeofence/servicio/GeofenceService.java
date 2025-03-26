package com.traccar.PositionGeofence.servicio;import java.util.List;
import com.traccar.PositionGeofence.modelo.Geofence;

public interface GeofenceService {
    List<Geofence> getAllGeofences();
    Geofence getGeofenceById(String id);
    Geofence createGeofence(Geofence geofence);
    Geofence updateGeofence(String id, Geofence geofence);
    void deleteGeofence(String id);
}
