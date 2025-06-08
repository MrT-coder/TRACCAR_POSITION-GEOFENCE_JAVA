package com.traccar.PositionGeofence.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.traccar.PositionGeofence.session.ConnectionKey;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class LogRecord {

    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;

    public LogRecord(SocketAddress localAddress, SocketAddress remoteAddress) {
        this.localAddress = (InetSocketAddress) localAddress;
        this.remoteAddress = (InetSocketAddress) remoteAddress;
    }

    @JsonIgnore
    public InetSocketAddress getAddress() {
        return remoteAddress;
    }

    @JsonIgnore
    public ConnectionKey getConnectionKey() {
        return new ConnectionKey(localAddress, remoteAddress);
    }

    public String getHost() {
        return remoteAddress.getHostString();
    }

    private String protocol;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    private String uniqueId;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    private long deviceId;

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}