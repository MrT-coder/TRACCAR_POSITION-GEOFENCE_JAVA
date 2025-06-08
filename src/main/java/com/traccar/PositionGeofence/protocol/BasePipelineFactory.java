package com.traccar.PositionGeofence.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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

/**
 * Factoria base de pipelines Netty, parametrizada por protocolo y con Spring
 * DI.
 */
@Component
public abstract class BasePipelineFactory extends ChannelInitializer<Channel> implements ApplicationContextAware {

    protected final TrackerConnector connector;
    protected final Config config;
    protected final String protocol;
    protected final int timeout;

    @Autowired
    private RemoteAddressHandler remoteAddressHandler;

    @Autowired
    private ProcessingHandler processingHandler;

    @Autowired
    private MainEventHandler mainEventHandler;

    @Autowired
    private ApplicationContext ctx;

    public BasePipelineFactory(TrackerConnector connector, Config config, String protocol) {
        this.connector = connector;
        this.config = config;
        this.protocol = protocol;
        int t = config.getInteger(Keys.PROTOCOL_TIMEOUT.withPrefix(protocol));
        if (t == 0) {
            this.timeout = config.getInteger(Keys.SERVER_TIMEOUT);
        } else {
            this.timeout = t;
        }
    }

    /**
     * Método auxiliar para que otras clases encuentren handlers ya añadidos al
     * pipeline.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ChannelHandler> T getHandler(ChannelPipeline pipeline, Class<T> clazz) {
        for (Map.Entry<String, ChannelHandler> handlerEntry : pipeline) {
            ChannelHandler handler = handlerEntry.getValue();
            if (handler instanceof WrapperInboundHandler wrapperHandler) {
                handler = wrapperHandler.getWrappedHandler();
            } else if (handler instanceof WrapperOutboundHandler wrapperHandler) {
                handler = wrapperHandler.getWrappedHandler();
            }
            if (clazz.isAssignableFrom(handler.getClass())) {
                return (T) handler;
            }
        }
        return null;
    }

    protected abstract void addTransportHandlers(PipelineBuilder pipeline);

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

    @Override
    protected void initChannel(Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        // 1) transporte puro (framing, SSL, etc)
        addTransportHandlers(pipeline::addLast);

        // 2) idle timeout
        if (!connector.isDatagram() && timeout > 0) {
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0));
        }

        // 3) canal abierto / forwarder / logging / mensaje bruto
        pipeline.addLast(new OpenChannelHandler(connector));
        if (config.hasKey(Keys.SERVER_FORWARD)) {
            int port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocol));
            pipeline.addLast(new NetworkForwarderHandler(port));
        }
        pipeline.addLast(new NetworkMessageHandler());
        StandardLoggingHandler loggingHandler = ctx.getBean(StandardLoggingHandler.class,protocol);
        pipeline.addLast(loggingHandler);

        // 4) ACK si toca
        if (!connector.isDatagram()
                && !config.getBoolean(Keys.SERVER_INSTANT_ACKNOWLEDGEMENT)) {
            pipeline.addLast(new AcknowledgementHandler());
        }

        // 5) protocolo concreto (decoder/encoder)
        addProtocolHandlers(pipeline::addLast);

        // 6) resto de la cadena de eventos
        pipeline.addLast(remoteAddressHandler);
        pipeline.addLast(processingHandler);
        pipeline.addLast(mainEventHandler);

    }

}