package com.traccar.PositionGeofence.geofence;

import java.text.ParseException;

public abstract class GeofenceGeometry {

    /**
     * Determina si el punto (latitude, longitude) se encuentra dentro de la geometría.
     * Se utiliza un objeto de configuración y la geocerca asociada, según la lógica del sistema.
     */
    public abstract boolean containsPoint(Object config, Object geofence, double latitude, double longitude);

    /**
     * Calcula el área de la geometría.
     */
    public abstract double calculateArea();

    /**
     * Retorna la representación en WKT de la geometría.
     */
    public abstract String toWkt();

    /**
     * Parsea la cadena WKT para inicializar la geometría.
     */
    public abstract void fromWkt(String wkt) throws ParseException;

    /**
     * Clase interna para representar una coordenada (latitud y longitud).
     */
    public static class Coordinate {
        private double lat;
        private double lon;

        public double getLat() {
            return lat;
        }
        public void setLat(double lat) {
            this.lat = lat;
        }
        public double getLon() {
            return lon;
        }
        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}
