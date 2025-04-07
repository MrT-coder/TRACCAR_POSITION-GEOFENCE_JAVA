package com.traccar.PositionGeofence.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.servicio.PositionService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    /**
     * GET /positions
     * Permite filtrar por deviceId y por rango de fechas, o listar todas las posiciones.
     *
     * Ejemplo: GET /positions?deviceId=123&from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z
     */
    @GetMapping
    public List<Position> getPositions(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
    ) {
        return positionService.getPositions(deviceId, from, to);
    }

    /**
     * GET /positions/{id} para obtener una posición por su ID.
     */
    @GetMapping("/{id}")
    public Position getPositionById(@PathVariable String id) {
        return positionService.getPositionById(id);
    }

    /**
     * POST /positions para crear una nueva posición.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Position createPosition(@RequestBody Position position) {
        return positionService.savePosition(position);
    }

    /**
     * DELETE /positions para eliminar posiciones por deviceId y rango de fechas.
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
        positionService.deletePositions(deviceId, from, to);
    }
}
