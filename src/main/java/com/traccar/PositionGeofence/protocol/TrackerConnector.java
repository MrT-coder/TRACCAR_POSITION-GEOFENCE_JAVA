package com.traccar.PositionGeofence.protocol;

import com.traccar.PositionGeofence.LifecycleObject;

import io.netty.channel.group.ChannelGroup;

public interface TrackerConnector extends LifecycleObject {

    boolean isDatagram();

    boolean isSecure();

    ChannelGroup getChannelGroup();

}
