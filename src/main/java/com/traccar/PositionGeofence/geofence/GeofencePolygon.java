package com.traccar.PositionGeofence.geofence;

import java.text.ParseException;
import java.util.ArrayList;

public class GeofencePolygon extends GeofenceGeometry {

    private ArrayList<Coordinate> coordinates;

    public GeofencePolygon() {
        coordinates = new ArrayList<>();
    }

    public GeofencePolygon(String wkt) throws ParseException {
        this();
        fromWkt(wkt);
    }

    // Calcula el área usando la fórmula de shoelace
    @Override
    public double calculateArea() {
        double area = 0;
        int n = coordinates.size();
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += coordinates.get(i).getLon() * coordinates.get(j).getLat();
            area -= coordinates.get(j).getLon() * coordinates.get(i).getLat();
        }
        return Math.abs(area / 2);
    }

    // Verifica si el punto está dentro del polígono usando el algoritmo de ray-casting
    @Override
    public boolean containsPoint(Object config, Object geofence, double latitude, double longitude) {
        boolean inside = false;
        int n = coordinates.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = coordinates.get(i).getLon();
            double yi = coordinates.get(i).getLat();
            double xj = coordinates.get(j).getLon();
            double yj = coordinates.get(j).getLat();

            boolean intersect = ((yi > latitude) != (yj > latitude)) &&
                    (longitude < (xj - xi) * (latitude - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    @Override
    public String toWkt() {
        StringBuilder buf = new StringBuilder("POLYGON ((");
        for (Coordinate coordinate : coordinates) {
            buf.append(coordinate.getLat()).append(" ").append(coordinate.getLon()).append(", ");
        }
        return buf.substring(0, buf.length() - 2) + "))";
    }

    @Override
    public void fromWkt(String wkt) throws ParseException {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        } else {
            coordinates.clear();
        }
        if (!wkt.startsWith("POLYGON")) {
            throw new ParseException("Mismatch geometry type", 0);
        }
        // Se espera un WKT con el formato "POLYGON ((lat lon, lat lon, ...))"
        String content = wkt.substring(wkt.indexOf("((") + 2, wkt.indexOf("))"));
        if (content.isEmpty()) {
            throw new ParseException("No content", 0);
        }
        String[] commaTokens = content.split(",");
        if (commaTokens.length < 3) {
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