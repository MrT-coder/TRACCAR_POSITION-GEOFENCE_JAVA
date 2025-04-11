package com.traccar.PositionGeofence.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.traccar.PositionGeofence.modelo.Geofence;
import com.traccar.PositionGeofence.servicio.GeofenceService;

@RestController
@RequestMapping("/api/geofences")
public class GeofenceController {

    @Autowired
    private GeofenceService geofenceService;

    // Endpoint para obtener todas las geocercas
    @GetMapping
    public ResponseEntity<List<Geofence>> getAllGeofences() {
        List<Geofence> geofences = geofenceService.getAllGeofences();
        return ResponseEntity.ok(geofences);
    }

    // Endpoint para obtener una geocerca por su ID
    @GetMapping("/{id}")
    public ResponseEntity<Geofence> getGeofenceById(@PathVariable long id) {
        Geofence geofence = geofenceService.getGeofenceById(id);
        if (geofence == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(geofence);
    }

    // Endpoint para crear una nueva geocerca
    @PostMapping
    public ResponseEntity<Geofence> createGeofence(@RequestBody Geofence geofence) {
        Geofence created = geofenceService.createGeofence(geofence);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Endpoint para actualizar una geocerca existente
    @PutMapping("/{id}")
    public ResponseEntity<Geofence> updateGeofence(@PathVariable long id, @RequestBody Geofence geofence) {
        Geofence updated = geofenceService.updateGeofence(id, geofence);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updated);
    }

    // Endpoint para eliminar una geocerca por su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGeofence(@PathVariable long id) {
        geofenceService.deleteGeofence(id);
        return ResponseEntity.noContent().build();
    }
}