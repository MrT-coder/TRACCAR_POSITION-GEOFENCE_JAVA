package com.traccar.PositionGeofence.repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.traccar.PositionGeofence.modelo.Position;

import java.util.Date;
import java.util.List;

@Repository
public interface PositionRepository extends MongoRepository<Position, String> {

    // Encuentra todas las posiciones de un dispositivo
    List<Position> findByDeviceId(Long deviceId);

    // Encuentra posiciones de un dispositivo en un rango de tiempo
    List<Position> findByDeviceIdAndFixTimeBetween(Long deviceId, Date from, Date to);

    // Elimina posiciones en un rango de tiempo
    void deleteByDeviceIdAndFixTimeBetween(Long deviceId, Date from, Date to);

    @SuppressWarnings("null")
    List<Position> findAll();
}
