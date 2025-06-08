package com.traccar.PositionGeofence.protocol;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public final class EventLoopGroupFactory {

    private static final EventLoopGroup BOSS_GROUP = new NioEventLoopGroup();
    private static final EventLoopGroup WORKER_GROUP = new NioEventLoopGroup();

    private EventLoopGroupFactory() {
    }

    public static EventLoopGroup getBossGroup() {
        return BOSS_GROUP;
    }

    public static EventLoopGroup getWorkerGroup() {
        return WORKER_GROUP;
    }

}
