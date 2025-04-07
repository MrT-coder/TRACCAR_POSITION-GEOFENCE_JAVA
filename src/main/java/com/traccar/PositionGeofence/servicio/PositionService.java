package com.traccar.PositionGeofence.servicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.traccar.PositionGeofence.mensajeria.PositionMessageProducer;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.repositorio.PositionRepository;

import java.util.Date;
import java.util.List;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionMessageProducer messageProducer;

    /**
     * Guarda la posición en la base de datos y la envía a RabbitMQ para que el microservicio de Events la procese.
     *
     * @param position la posición a guardar
     * @return la posición guardada
     */
    public Position savePosition(Position position) {
        // Persistir la posición en MongoDB
        Position saved = positionRepository.save(position);
        // Publicar la posición en RabbitMQ para que el microservicio de Events la consuma
        messageProducer.sendPositionMessage(saved);
        return saved;
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
    public Position getPositionById(String id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
    }
}