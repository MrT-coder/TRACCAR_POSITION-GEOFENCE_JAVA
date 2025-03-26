package com.traccar.PositionGeofence.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.traccar.PositionGeofence.modelo.Geofence;
import com.traccar.PositionGeofence.repositorio.GeofenceRepository;

@Service
public class GeofenceServiceImpl implements GeofenceService {

    @Autowired
    private GeofenceRepository geofenceRepository;

    @Override
    public List<Geofence> getAllGeofences() {
        return geofenceRepository.findAll();
    }

    @Override
    public Geofence getGeofenceById(String id) {
        Optional<Geofence> optional = geofenceRepository.findById(id);
        return optional.orElse(null); // Podrías lanzar una excepción personalizada si no se encuentra
    }

    @Override
    public Geofence createGeofence(Geofence geofence) {
        return geofenceRepository.save(geofence);
    }

    @Override
    public Geofence updateGeofence(String id, Geofence geofence) {
        // Primero validamos que exista la geocerca
        if (!geofenceRepository.existsById(id)) {
            return null;
        }
        geofence.setId(id);
        return geofenceRepository.save(geofence);
    }

    @Override
    public void deleteGeofence(String id) {
        geofenceRepository.deleteById(id);
    }
}
