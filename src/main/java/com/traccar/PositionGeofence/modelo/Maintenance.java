package com.traccar.PositionGeofence.modelo;

public class Maintenance extends ExtendedModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private double start;

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    private double period;

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

}
