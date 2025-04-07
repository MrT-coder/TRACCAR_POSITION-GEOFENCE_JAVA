package com.traccar.PositionGeofence.modelo;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Device extends GroupedModel implements Disableable, Schedulable  {
 private long calendarId;

    @Override
    public long getCalendarId() {
        return calendarId;
    }

    @Override
    public void setCalendarId(long calendarId) {
        this.calendarId = calendarId;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String uniqueId;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        if (uniqueId.contains("..")) {
            throw new IllegalArgumentException("Invalid unique id");
        }
        this.uniqueId = uniqueId.trim();
    }

    public static final String STATUS_UNKNOWN = "unknown";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";

    private String status;

  
    public String getStatus() {
        return status != null ? status : STATUS_OFFLINE;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : null;
    }

    private Date lastUpdate;

  
    public Date getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    private long positionId;

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    private String contact;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private boolean disabled;

    @Override
    public boolean getDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    private Date expirationTime;

    @Override
    public Date getExpirationTime() {
        return expirationTime;
    }

    @Override
    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    private boolean motionStreak;


    @JsonIgnore
    public boolean getMotionStreak() {
        return motionStreak;
    }

    @JsonIgnore
    public void setMotionStreak(boolean motionStreak) {
        this.motionStreak = motionStreak;
    }

    private boolean motionState;

 
    @JsonIgnore
    public boolean getMotionState() {
        return motionState;
    }

    @JsonIgnore
    public void setMotionState(boolean motionState) {
        this.motionState = motionState;
    }

    private Date motionTime;


    @JsonIgnore
    public Date getMotionTime() {
        return motionTime;
    }

    @JsonIgnore
    public void setMotionTime(Date motionTime) {
        this.motionTime = motionTime;
    }

    private double motionDistance;


    @JsonIgnore
    public double getMotionDistance() {
        return motionDistance;
    }

    @JsonIgnore
    public void setMotionDistance(double motionDistance) {
        this.motionDistance = motionDistance;
    }

    private boolean overspeedState;


    @JsonIgnore
    public boolean getOverspeedState() {
        return overspeedState;
    }

    @JsonIgnore
    public void setOverspeedState(boolean overspeedState) {
        this.overspeedState = overspeedState;
    }

    private Date overspeedTime;


    @JsonIgnore
    public Date getOverspeedTime() {
        return overspeedTime;
    }

    @JsonIgnore
    public void setOverspeedTime(Date overspeedTime) {
        this.overspeedTime = overspeedTime;
    }

    private long overspeedGeofenceId;


    @JsonIgnore
    public long getOverspeedGeofenceId() {
        return overspeedGeofenceId;
    }

    @JsonIgnore
    public void setOverspeedGeofenceId(long overspeedGeofenceId) {
        this.overspeedGeofenceId = overspeedGeofenceId;
    }
}
