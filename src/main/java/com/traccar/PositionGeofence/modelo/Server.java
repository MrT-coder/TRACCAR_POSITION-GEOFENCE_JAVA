package com.traccar.PositionGeofence.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Server extends ExtendedModel implements UserRestrictions {

    private boolean registration;

    public boolean getRegistration() {
        return registration;
    }

    public void setRegistration(boolean registration) {
        this.registration = registration;
    }

    private boolean readonly;

    @Override
    public boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    private boolean deviceReadonly;

    @Override
    public boolean getDeviceReadonly() {
        return deviceReadonly;
    }

    public void setDeviceReadonly(boolean deviceReadonly) {
        this.deviceReadonly = deviceReadonly;
    }

    private String map;

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    private String bingKey;

    public String getBingKey() {
        return bingKey;
    }

    public void setBingKey(String bingKey) {
        this.bingKey = bingKey;
    }

    private String mapUrl;

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    private String overlayUrl;

    public String getOverlayUrl() {
        return overlayUrl;
    }

    public void setOverlayUrl(String overlayUrl) {
        this.overlayUrl = overlayUrl;
    }

    private double latitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    private double longitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    private int zoom;

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    private boolean forceSettings;

    public boolean getForceSettings() {
        return forceSettings;
    }

    public void setForceSettings(boolean forceSettings) {
        this.forceSettings = forceSettings;
    }

    private String coordinateFormat;

    public String getCoordinateFormat() {
        return coordinateFormat;
    }

    public void setCoordinateFormat(String coordinateFormat) {
        this.coordinateFormat = coordinateFormat;
    }

    private boolean limitCommands;

    @Override
    public boolean getLimitCommands() {
        return limitCommands;
    }

    public void setLimitCommands(boolean limitCommands) {
        this.limitCommands = limitCommands;
    }

    private boolean disableReports;

    @Override
    public boolean getDisableReports() {
        return disableReports;
    }

    public void setDisableReports(boolean disableReports) {
        this.disableReports = disableReports;
    }

    private boolean fixedEmail;

    @Override
    public boolean getFixedEmail() {
        return fixedEmail;
    }

    public void setFixedEmail(boolean fixedEmail) {
        this.fixedEmail = fixedEmail;
    }

    private String poiLayer;

    public String getPoiLayer() {
        return poiLayer;
    }

    public void setPoiLayer(String poiLayer) {
        this.poiLayer = poiLayer;
    }

    private String announcement;

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }


    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    private boolean emailEnabled;


    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }


    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    private boolean geocoderEnabled;

    private boolean textEnabled;


    public void setTextEnabled(boolean textEnabled) {
        this.textEnabled = textEnabled;
    }


    public Boolean getTextEnabled() {
        return textEnabled;
    }


    public void setGeocoderEnabled(boolean geocoderEnabled) {
        this.geocoderEnabled = geocoderEnabled;
    }


    public boolean getGeocoderEnabled() {
        return geocoderEnabled;
    }

    private long[] storageSpace;


    public long[] getStorageSpace() {
        return storageSpace;
    }


    public void setStorageSpace(long[] storageSpace) {
        this.storageSpace = storageSpace;
    }

    private boolean newServer;


    public boolean getNewServer() {
        return newServer;
    }


    public void setNewServer(boolean newServer) {
        this.newServer = newServer;
    }

    private boolean openIdEnabled;


    public boolean getOpenIdEnabled() {
        return openIdEnabled;
    }


    public void setOpenIdEnabled(boolean openIdEnabled) {
        this.openIdEnabled = openIdEnabled;
    }

    private boolean openIdForce;


    public boolean getOpenIdForce() {
        return openIdForce;
    }


    public void setOpenIdForce(boolean openIdForce) {
        this.openIdForce = openIdForce;
    }
}
