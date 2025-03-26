package com.traccar.PositionGeofence.modelo;

import java.text.ParseException;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.traccar.PositionGeofence.geofence.GeofenceGeometry;
import com.traccar.PositionGeofence.geofence.GeofenceGeometryFactory;

@Document(collection = "mcs_geofence")
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

    private Map<String, Object> attributes;

    // Variable interna que almacena la geometría ya parseada
    @JsonIgnore
    private GeofenceGeometry geometry;
    // Constructor vacío requerido por Spring
    public Geofence() {
    }

    // Constructor con parámetros (opcional)
    public Geofence(long calendarId, String name, String description, String area) throws ParseException {
        this.calendarId = calendarId;
        this.name = name;
        this.description = description;
        setArea(area);
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

    // Aquí se integra la lógica para generar la geometría a partir del string WKT
    public void setArea(String area) throws ParseException {
        this.geometry = GeofenceGeometryFactory.parse(area);
        this.area = area;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

     // Permite obtener la geometría ya parseada (no se expone en la respuesta JSON)
    @JsonIgnore
    public GeofenceGeometry getGeometry() {
        return geometry;
    }

    // Permite establecer la geometría directamente y actualiza el campo "area" con su representación WKT
    @JsonIgnore
    public void setGeometry(GeofenceGeometry geometry) {
        this.area = geometry.toWkt();
        this.geometry = geometry;
    }
}
