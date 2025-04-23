package com.traccar.PositionGeofence.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import com.google.inject.Injector;

import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.BaseProtocolDecoder;
import com.traccar.PositionGeofence.PositionGeofenceApplication;
import com.traccar.PositionGeofence.ProcessingHandler;
import com.traccar.PositionGeofence.TrackerConnector;
import com.traccar.PositionGeofence.WrapperInboundHandler;
import com.traccar.PositionGeofence.WrapperOutboundHandler;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.handler.network.AcknowledgementHandler;
import com.traccar.PositionGeofence.handler.network.MainEventHandler;
import com.traccar.PositionGeofence.handler.network.NetworkForwarderHandler;
import com.traccar.PositionGeofence.handler.network.NetworkMessageHandler;
import com.traccar.PositionGeofence.handler.network.OpenChannelHandler;
import com.traccar.PositionGeofence.handler.network.RemoteAddressHandler;
import com.traccar.PositionGeofence.handler.network.StandardLoggingHandler;

import java.util.Map;

@Component
public abstract class BasePipelineFactory extends ChannelInitializer<Channel> {

    private final Injector injector;
    private final TrackerConnector connector;
    private final Config config;
    private final String protocol;
    private final int timeout;

  public BasePipelineFactory(TrackerConnector connector, Config config, String protocol) {
        this.injector = PositionGeofenceApplication.getInjector();
        this.connector = connector;
        this.config = config;
        this.protocol = protocol;
        int timeout = config.getInteger(Keys.PROTOCOL_TIMEOUT.withPrefix(protocol));
        if (timeout == 0) {
            this.timeout = config.getInteger(Keys.SERVER_TIMEOUT);
        } else {
            this.timeout = timeout;
        }
    }
   
    /**
     * Método abstracto para agregar handlers de transporte (por ejemplo, SSL, decodificadores de frames, etc.).
     */
    protected abstract void addTransportHandlers(PipelineBuilder pipeline);

    /**
     * Método abstracto para agregar los handlers específicos del protocolo (decoders/encoders).
     */
    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

 

    /**
     * Método utilitario para obtener un handler específico del pipeline.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ChannelHandler> T getHandler(ChannelPipeline pipeline, Class<T> clazz) {
        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            ChannelHandler handler = entry.getValue();
            if (handler instanceof WrapperInboundHandler) {
                handler = ((WrapperInboundHandler) handler).getWrappedHandler();
            } else if (handler instanceof WrapperOutboundHandler) {
                handler = ((WrapperOutboundHandler) handler).getWrappedHandler();
            }
            if (clazz.isAssignableFrom(handler.getClass())) {
                return (T) handler;
            }
        }
        return null;
    }

    private <T> T injectMembers(T object) {
        injector.injectMembers(object);
        return object;
    }

    @Override
    protected void initChannel(Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        addTransportHandlers(pipeline::addLast);

        if (timeout > 0 && !connector.isDatagram()) {
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0));
        }
        pipeline.addLast(new OpenChannelHandler(connector));
        if (config.hasKey(Keys.SERVER_FORWARD)) {
            int port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocol));
            pipeline.addLast(injectMembers(new NetworkForwarderHandler(port)));
        }
        pipeline.addLast(new NetworkMessageHandler());
        pipeline.addLast(injectMembers(new StandardLoggingHandler(protocol)));

        if (!connector.isDatagram() && !config.getBoolean(Keys.SERVER_INSTANT_ACKNOWLEDGEMENT)) {
            pipeline.addLast(new AcknowledgementHandler());
        }

        addProtocolHandlers(handler -> {
            if (handler instanceof BaseProtocolDecoder || handler instanceof BaseProtocolEncoder) {
                injectMembers(handler);
            } else {
                if (handler instanceof ChannelInboundHandler channelHandler) {
                    handler = new WrapperInboundHandler(channelHandler);
                } else if (handler instanceof ChannelOutboundHandler channelHandler) {
                    handler = new WrapperOutboundHandler(channelHandler);
                }
            }
            pipeline.addLast(handler);
        });

        pipeline.addLast(injector.getInstance(RemoteAddressHandler.class));
        pipeline.addLast(injector.getInstance(ProcessingHandler.class));
        pipeline.addLast(injector.getInstance(MainEventHandler.class));
    }
}