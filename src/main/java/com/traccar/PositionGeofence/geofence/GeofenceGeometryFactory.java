package com.traccar.PositionGeofence.geofence;

import java.text.ParseException;

public class GeofenceGeometryFactory {

    public static GeofenceGeometry parse(String area) throws ParseException {
        if (area.startsWith("CIRCLE")) {
            return new GeofenceCircle(area);
        } else if (area.startsWith("POLYGON")) {
            return new GeofencePolygon(area);
        } else if (area.startsWith("LINESTRING")) {
            return new GeofencePolyline(area);
        } else {
            throw new ParseException("Unknown geometry type", 0);
        }
    }
}
