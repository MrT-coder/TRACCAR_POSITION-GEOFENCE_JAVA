package com.traccar.PositionGeofence.handler.network;

import com.traccar.PositionGeofence.TrackerConnector;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class OpenChannelHandler extends ChannelDuplexHandler {

    private final TrackerConnector connector;

    public OpenChannelHandler(TrackerConnector connector) {
        this.connector = connector;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        connector.getChannelGroup().add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        connector.getChannelGroup().remove(ctx.channel());
    }
}