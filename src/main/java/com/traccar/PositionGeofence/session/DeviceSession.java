package com.traccar.PositionGeofence.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequestDecoder;


import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.modelo.Command;
import com.traccar.PositionGeofence.protocol.BasePipelineFactory;

public class DeviceSession {

    private final long deviceId;
    private final String uniqueId;
    private final String model;
    private final Protocol protocol;
    private final Channel channel;
    private final SocketAddress remoteAddress;

    public DeviceSession(
            long deviceId, String uniqueId, String model,
            Protocol protocol, Channel channel, SocketAddress remoteAddress) {
        this.deviceId = deviceId;
        this.uniqueId = uniqueId;
        this.model = model;
        this.protocol = protocol;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getModel() {
        return model;
    }

    public Channel getChannel() {
        return channel;
    }

    public ConnectionKey getConnectionKey() {
        return new ConnectionKey(channel, remoteAddress);
    }

    // public boolean supportsLiveCommands() {
    //     return BasePipelineFactory.getHandler(channel.pipeline(), HttpRequestDecoder.class) == null;
    // }

    // public void sendCommand(Command command) {
    //     protocol.sendDataCommand(channel, remoteAddress, command);
    // }

    public static final String KEY_TIMEZONE = "timezone";

    private final Map<String, Object> locals = new HashMap<>();

    public boolean contains(String key) {
        return locals.containsKey(key);
    }

    public void set(String key, Object value) {
        if (value != null) {
            locals.put(key, value);
        } else {
            locals.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) locals.get(key);
    }

}