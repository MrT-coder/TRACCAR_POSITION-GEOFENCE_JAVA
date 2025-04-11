package com.traccar.PositionGeofence.servicio;
import java.util.List;
import com.traccar.PositionGeofence.modelo.Geofence;

public interface GeofenceService {
    List<Geofence> getAllGeofences();
    Geofence getGeofenceById(Long id);
    Geofence createGeofence(Geofence geofence);
    Geofence updateGeofence(Long id, Geofence geofence);
    void deleteGeofence(Long id);
}
