package com.traccar.PositionGeofence.protocol;

import io.netty.channel.ChannelHandler;

public interface PipelineBuilder {
    void addLast(ChannelHandler handler);
}
