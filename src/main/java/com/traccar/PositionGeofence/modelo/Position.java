package com.traccar.PositionGeofence.modelo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mcs_positions")
public class Position {
    @Id
    private String id;

    private Long deviceId;

    private String protocol;
    private Date serverTime = new Date();
    private Date deviceTime;
    private Date fixTime;
    
    private boolean outdated;
    private boolean valid;
    
    // Para aprovechar índices geoespaciales, se recomienda usar un objeto GeoJSON
    // Por simplicidad, aquí mantenemos latitude y longitude, pero más adelante podrías
    // transformar estos campos en un objeto "location" de tipo GeoJSON.
    private double latitude;
    private double longitude;
    
    private double altitude;   // metros
    private double speed;      // en la unidad que decidas (p. ej., km/h o nudos)
    private double course;
    
    private String address;
    private double accuracy;
    
    // Si Network es complejo, podrías modelarlo como un objeto o como un Map
    private Map<String, Object> network;
    
    // Lista de IDs de geocercas asociadas a esta posición.
    private List<Long> geofenceIds;
    
    // Atributos adicionales en forma de key-value
    private Map<String, Object> attributes;

    // Constructores
    public Position() {
    }

    public Position(String protocol, Date deviceTime, Date fixTime, double latitude, double longitude) {
        this.protocol = protocol;
        this.deviceTime = deviceTime;
        this.fixTime = fixTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters y setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
       this.id = id;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public void setServerTime(Date serverTime) {
        this.serverTime = serverTime;
    }

    public Date getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(Date deviceTime) {
        this.deviceTime = deviceTime;
    }

    public Date getFixTime() {
        return fixTime;
    }

    public void setFixTime(Date fixTime) {
        this.fixTime = fixTime;
    }

    public boolean isOutdated() {
        return outdated;
    }

    public void setOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude out of range");
        }
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude out of range");
        }
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCourse() {
        return course;
    }

    public void setCourse(double course) {
        this.course = course;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Map<String, Object> getNetwork() {
        return network;
    }

    public void setNetwork(Map<String, Object> network) {
        this.network = network;
    }

    public List<Long> getGeofenceIds() {
        return geofenceIds;
    }

    public void setGeofenceIds(List<Long> geofenceIds) {
        this.geofenceIds = geofenceIds;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}