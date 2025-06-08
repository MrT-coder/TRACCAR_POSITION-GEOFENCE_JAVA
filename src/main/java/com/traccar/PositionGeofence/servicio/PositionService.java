package com.traccar.PositionGeofence.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.mensajeria.PositionMessageProducer;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.repositorio.PositionRepository;
import com.traccar.PositionGeofence.session.cache.CacheManager;

import java.util.Date;
import java.util.List;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionMessageProducer messageProducer;

    private final CacheManager cacheManager;

    public PositionService(PositionRepository positionRepository, CacheManager cacheManager) {
        this.positionRepository = positionRepository;
        this.cacheManager = cacheManager;
    }
    /**
     * Guarda la posición en MongoDB y actualiza la caché.
     *
     * @param position El objeto Position a almacenar.
     * @return La posición guardada (con el id asignado, si corresponde).
     */
    public Position savePosition(Position position) {
        Position savedPosition = positionRepository.save(position);
        // Actualiza la última posición del dispositivo en la caché
        cacheManager.updatePosition(savedPosition);
        messageProducer.sendPositionMessage(savedPosition);
        return savedPosition;
    }
    
    
    /**
     * Obtiene posiciones filtradas por deviceId y, opcionalmente, por un rango de fechas.
     */
    public List<Position> getPositions(Long deviceId, Date from, Date to) {
        if (deviceId != null && from != null && to != null) {
            return positionRepository.findByDeviceIdAndFixTimeBetween(deviceId, from, to);
        } else if (deviceId != null) {
            return positionRepository.findByDeviceId(deviceId);
        } else {
            return positionRepository.findAll();
        }
    }
    
    /**
     * Elimina las posiciones para un deviceId en un rango de fechas.
     */
    public void deletePositions(Long deviceId, Date from, Date to) {
        positionRepository.deleteByDeviceIdAndFixTimeBetween(deviceId, from, to);
    }
    
    /**
     * Obtiene una posición por su ID.
     */
    public Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
    }
}