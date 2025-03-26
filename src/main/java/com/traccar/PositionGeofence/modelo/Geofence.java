package com.traccar.PositionGeofence.modelo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mcs_geofences")
public class Geofence {

    @Id
    private String id;

    // Identificador de calendario (si tu sistema maneja calendarios para activar/desactivar la geocerca)
    private long calendarId;

    // Nombre de la geocerca
    private String name;

    // Descripción de la geocerca
    private String description;

    /**
     * Representación del área en formato WKT (por ejemplo: 
     * "CIRCLE (lat lon, radius)" o "POLYGON((...))" o "LINESTRING(...)").
     * Puedes almacenar directamente este string o parsearlo para manejar geometría real.
     */
    private String area;

    // Constructor vacío requerido por Spring
    public Geofence() {
    }

    // Constructor con parámetros (opcional)
    public Geofence(long calendarId, String name, String description, String area) {
        this.calendarId = calendarId;
        this.name = name;
        this.description = description;
        this.area = area;
    }

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(long calendarId) {
        this.calendarId = calendarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        // Aquí podrías parsear el texto para identificar 
        // si es CIRCLE, POLYGON, LINESTRING, etc.
        this.area = area;
    }
}
