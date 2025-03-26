package com.traccar.PositionGeofence.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.repositorio.PositionRepository;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionRepository positionRepository;

    /**
     * GET /positions
     * Permite filtrar por deviceId, rango de fechas (from, to), o listar todas las posiciones.
     *
     * Ejemplo de uso:
     * GET /positions?deviceId=123&from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z
     */
    @GetMapping
    public List<Position> getPositions(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
    ) {

        // Caso 1: si se pasan deviceId, from y to => filtra por rango
        if (deviceId != null && from != null && to != null) {
            // Ejemplo: un método custom en el repositorio:
            // List<Position> findByDeviceIdAndFixTimeBetween(Long deviceId, Date start, Date end);
            return positionRepository.findByDeviceIdAndFixTimeBetween(deviceId, from, to);

        // Caso 2: si se pasa solo deviceId => obtener las últimas posiciones de ese dispositivo
        } else if (deviceId != null) {
            // Podrías decidir retornar todas las posiciones del device
            // o solo las más recientes. Depende de tu lógica
            return positionRepository.findByDeviceId(deviceId);

        // Caso 3: si no se pasa nada => retorna todas las posiciones
        } else {
            return positionRepository.findAll();
        }
    }

    /**
     * DELETE /positions
     * Recibe deviceId y un rango de fechas (from, to) para borrar posiciones.
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePositions(
            @RequestParam Long deviceId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
    ) {
        // En un escenario real, podrías verificar permisos o restricciones aquí
        // y luego llamar a un método del repositorio o un servicio que haga la eliminación.

        // Ejemplo de método custom en el repositorio:
        // void deleteByDeviceIdAndFixTimeBetween(Long deviceId, Date start, Date end);
        positionRepository.deleteByDeviceIdAndFixTimeBetween(deviceId, from, to);
    }

    /**
     * Ejemplo adicional: GET /positions/{id} para obtener una posición por su ID
     */
    @GetMapping("/{id}")
    public Position getPositionById(@PathVariable String id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Position not found"));
    }

    /**
     * Ejemplo adicional: POST /positions para crear una posición nueva
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Position createPosition(@RequestBody Position position) {
        // Aquí podrías realizar validaciones adicionales
        return positionRepository.save(position);
    }
    
    // Podrías agregar endpoints para exportar CSV, KML, GPX, etc. si deseas
    // reimplementar esa lógica de exportación.
}