package com.traccar.PositionGeofence.geofence;

import java.text.DecimalFormat;
import java.text.ParseException;

public class GeofenceCircle extends GeofenceGeometry {

    private double centerLatitude;
    private double centerLongitude;
    private double radius; // Se asume que el radio está en kilómetros

    public GeofenceCircle() {
    }

    public GeofenceCircle(String wkt) throws ParseException {
        fromWkt(wkt);
    }

    public GeofenceCircle(double latitude, double longitude, double radius) {
        this.centerLatitude = latitude;
        this.centerLongitude = longitude;
        this.radius = radius;
    }

    /**
     * Calcula la distancia entre el centro de la geocerca y el punto dado utilizando la fórmula de Haversine.
     *
     * @param latitude  latitud del punto
     * @param longitude longitud del punto
     * @return distancia en kilómetros
     */
    public double distanceFromCenter(double latitude, double longitude) {
        final int EARTH_RADIUS_KM = 6371;
        double latDistance = Math.toRadians(latitude - centerLatitude);
        double lonDistance = Math.toRadians(longitude - centerLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(centerLatitude)) * Math.cos(Math.toRadians(latitude))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public boolean containsPoint(Object config, Object geofence, double latitude, double longitude) {
        // Determina si la distancia al punto es menor o igual al radio
        return distanceFromCenter(latitude, longitude) <= radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public String toWkt() {
        StringBuilder wkt = new StringBuilder("CIRCLE (");
        wkt.append(centerLatitude).append(" ").append(centerLongitude).append(", ");
        DecimalFormat format = new DecimalFormat("0.#");
        wkt.append(format.format(radius)).append(")");
        return wkt.toString();
    }

    @Override
    public void fromWkt(String wkt) throws ParseException {
        if (!wkt.startsWith("CIRCLE")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        String content = wkt.substring(wkt.indexOf("(") + 1, wkt.indexOf(")"));
        if (content.isEmpty()) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length != 2) {
            throw new ParseException("Not valid content", 0);
        }
        String[] tokens = commaTokens[0].trim().split("\\s+");
        if (tokens.length != 2) {
            throw new ParseException("Too many or too few coordinates", 0);
        }
        try {
            centerLatitude = Double.parseDouble(tokens[0]);
        } catch (NumberFormatException e) {
            throw new ParseException(tokens[0] + " is not a double", 0);
        }
        try {
            centerLongitude = Double.parseDouble(tokens[1]);
        } catch (NumberFormatException e) {
            throw new ParseException(tokens[1] + " is not a double", 0);
        }
        try {
            radius = Double.parseDouble(commaTokens[1].trim());
        } catch (NumberFormatException e) {
            throw new ParseException(commaTokens[1] + " is not a double", 0);
        }
    }
}