package com.traccar.PositionGeofence.helper;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;

public final class NetworkUtil {

    private NetworkUtil() {
    }

    public static String session(Channel channel) {
        char transport = channel instanceof DatagramChannel ? 'U' : 'T';
        return transport + channel.id().asShortText();
    }

}