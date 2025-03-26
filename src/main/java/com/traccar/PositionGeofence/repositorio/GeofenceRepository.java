package com.traccar.PositionGeofence.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.traccar.PositionGeofence.modelo.Geofence;

public interface GeofenceRepository extends MongoRepository<Geofence, String> {
    // Aquí puedes definir consultas personalizadas si las necesitas, por ejemplo:
    List<Geofence> findByCalendarId(long calendarId);
    List<Geofence> findByName(String name);
    
}
