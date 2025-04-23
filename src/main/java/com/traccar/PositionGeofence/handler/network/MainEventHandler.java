package com.traccar.PositionGeofence.handler.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.BaseProtocolDecoder;
import com.traccar.PositionGeofence.helper.NetworkUtil;
import com.traccar.PositionGeofence.protocol.BasePipelineFactory;
import com.traccar.PositionGeofence.session.ConnectionManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@ChannelHandler.Sharable
public class MainEventHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainEventHandler.class);

    private final ConnectionManager connectionManager;
    private final Set<String> connectionlessProtocols = new HashSet<>();

    /**
     * Se inyecta la lista de protocolos connectionless desde la propiedad "status.ignore.offline".
     * Por ejemplo, en application.properties:
     *   status.ignore.offline=protocolA, protocolB
     */
    public MainEventHandler(ConnectionManager connectionManager,
                            @Value("${status.ignore.offline:}") String connectionlessProtocolList) {
        this.connectionManager = connectionManager;
        if (connectionlessProtocolList != null && !connectionlessProtocolList.trim().isEmpty()) {
            connectionlessProtocols.addAll(Arrays.asList(connectionlessProtocolList.split("[, ]+")));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (!(ctx.channel() instanceof DatagramChannel)) {
            LOGGER.info("[{}] connected", NetworkUtil.session(ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("[{}] disconnected", NetworkUtil.session(ctx.channel()));
        closeChannel(ctx.channel());

        // Determina si el canal soporta un estado offline: 
        // Se comprueba que no tenga un HttpRequestDecoder (lo que indica que es un canal HTTP) 
        // y que el protocolo no esté en la lista de protocolos connectionless.
        BaseProtocolDecoder protocolDecoder = BasePipelineFactory.getHandler(ctx.pipeline(), BaseProtocolDecoder.class);
        boolean supportsOffline = BasePipelineFactory.getHandler(ctx.pipeline(), HttpRequestDecoder.class) == null
                && (protocolDecoder == null || !connectionlessProtocols.contains(protocolDecoder.getProtocolName()));
        connectionManager.deviceDisconnected(ctx.channel(), supportsOffline);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        LOGGER.warn("[{}] error", NetworkUtil.session(ctx.channel()), cause);
        closeChannel(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            LOGGER.info("[{}] timed out", NetworkUtil.session(ctx.channel()));
            closeChannel(ctx.channel());
        }
    }

    private void closeChannel(Channel channel) {
        if (!(channel instanceof DatagramChannel)) {
            channel.close();
        }
    }
}