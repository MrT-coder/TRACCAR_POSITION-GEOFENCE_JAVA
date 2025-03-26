package com.traccar.PositionGeofence.geofence;

import java.text.ParseException;
import java.util.ArrayList;


public class GeofencePolyline extends GeofenceGeometry {

    private ArrayList<Coordinate> coordinates;

    // Umbral de distancia en kilómetros para considerar que el punto se encuentra sobre la línea
    private static final double POLYLINE_DISTANCE_THRESHOLD_KM = 0.01; // 10 metros aproximadamente

    public GeofencePolyline() {
        coordinates = new ArrayList<>();
    }

    public GeofencePolyline(String wkt) throws ParseException {
        this();
        fromWkt(wkt);
    }

    /**
     * Calcula la distancia mínima (en kilómetros) desde el punto (lat, lon) al segmento de línea
     * definido por los puntos (lat1, lon1) y (lat2, lon2) usando una aproximación plana.
     */
    private double distanceToLine(double lat, double lon,
                                  double lat1, double lon1,
                                  double lat2, double lon2) {
        double dx = lon2 - lon1;
        double dy = lat2 - lat1;
        double magSq = dx * dx + dy * dy;
        if (magSq == 0) {
            // El segmento es un punto
            double dlon = lon - lon1;
            double dlat = lat - lat1;
            return Math.sqrt(dlon * dlon + dlat * dlat) * 111; // Aproximación: 1 grado ~111 km
        }
        double t = ((lon - lon1) * dx + (lat - lat1) * dy) / magSq;
        if (t < 0) {
            t = 0;
        } else if (t > 1) {
            t = 1;
        }
        double projLon = lon1 + t * dx;
        double projLat = lat1 + t * dy;
        double dlon = lon - projLon;
        double dlat = lat - projLat;
        double distanceDegrees = Math.sqrt(dlon * dlon + dlat * dlat);
        return distanceDegrees * 111; // Conversión a kilómetros
    }

    @Override
    public boolean containsPoint(Object config, Object geofence, double latitude, double longitude) {
        // Itera sobre cada segmento de la polilínea
        for (int i = 1; i < coordinates.size(); i++) {
            double dist = distanceToLine(
                    latitude, longitude,
                    coordinates.get(i - 1).getLat(), coordinates.get(i - 1).getLon(),
                    coordinates.get(i).getLat(), coordinates.get(i).getLon());
            if (dist <= POLYLINE_DISTANCE_THRESHOLD_KM) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double calculateArea() {
        // Las polilíneas no tienen área
        return 0;
    }

    @Override
    public String toWkt() {
        StringBuilder buf = new StringBuilder("LINESTRING (");
        for (Coordinate coordinate : coordinates) {
            buf.append(coordinate.getLat())
               .append(" ")
               .append(coordinate.getLon())
               .append(", ");
        }
        // Quitar la última coma y espacio, y cerrar el paréntesis
        return buf.substring(0, buf.length() - 2) + ")";
    }

    @Override
    public void fromWkt(String wkt) throws ParseException {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        } else {
            coordinates.clear();
        }
        if (!wkt.startsWith("LINESTRING")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        String content = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")"));
        if (content.isEmpty()) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length < 2) {
            throw new ParseException("Not valid content", 0);
        }
        for (String commaToken : commaTokens) {
            String[] tokens = commaToken.trim().split("\\s+");
            if (tokens.length != 2) {
                throw new ParseException("Each coordinate must have two values: " + commaToken, 0);
            }
            Coordinate coordinate = new Coordinate();
            try {
                coordinate.setLat(Double.parseDouble(tokens[0]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[0] + " is not a double", 0);
            }
            try {
                coordinate.setLon(Double.parseDouble(tokens[1]));
            } catch (NumberFormatException e) {
                throw new ParseException(tokens[1] + " is not a double", 0);
            }
            coordinates.add(coordinate);
        }
    }
}