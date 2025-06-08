package com.traccar.PositionGeofence.handler.network;


import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.modelo.Position;

import java.net.InetSocketAddress;

@Component
@ChannelHandler.Sharable
public class RemoteAddressHandler extends ChannelInboundHandlerAdapter {

    private final boolean enabled;

    // Se inyecta la propiedad directamente. Por ejemplo, en application.properties:
    // processing.remote.address.enable=true
    public RemoteAddressHandler(@Value("${processing.remote.address.enable:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (enabled) {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String hostAddress = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : null;
            if (msg instanceof Position position) {
                position.set(Position.KEY_IP, hostAddress);
            }
        }
        ctx.fireChannelRead(msg);
    }
}