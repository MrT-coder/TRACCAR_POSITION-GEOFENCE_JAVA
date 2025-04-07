package com.traccar.PositionGeofence.protocol;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.ProcessingHandler;
import com.traccar.PositionGeofence.WrapperInboundHandler;
import com.traccar.PositionGeofence.WrapperOutboundHandler;
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

    // Inyección directa de propiedades desde application.properties
    @Value("${tracker.protocol.name}")
    protected String protocol;

    @Value("${tracker.protocol.ssl:false}")
    protected boolean secure;

    @Value("${tracker.protocol.timeout:0}")
    protected int protocolTimeout;

    @Value("${server.timeout:30}")
    protected int serverTimeout;

    @Value("${tracker.protocol.port}")
    protected int protocolPort;

    @Value("${tracker.server.forward:false}")
    protected boolean serverForward;

    @Value("${server.instant.ack:false}")
    protected boolean serverInstantAck;

    @Value("${tracker.protocol.datagram:false}")
    protected boolean datagram;

    @Autowired
    protected TrackerConnector connector;

    @Autowired
    protected AutowireCapableBeanFactory beanFactory;

    // Bootstrap de Netty que se configura en init()
    protected AbstractBootstrap<?, ?> bootstrap;

    // Timeout efectivo (si protocolTimeout es 0 se usa serverTimeout)
    protected int timeout;

    public BasePipelineFactory(TrackerServer trackerServer, boolean secure2) {
        //TODO Auto-generated constructor stub
    }

    @PostConstruct
    private void init() {
        // Configura el timeout efectivo
        this.timeout = (protocolTimeout == 0 ? serverTimeout : protocolTimeout);

        // Configura el bootstrap según si se usa UDP (datagram) o TCP
        if (datagram) {
            bootstrap = new Bootstrap()
                    .group(EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioDatagramChannel.class)
                    .handler(this);
        } else {
            bootstrap = new ServerBootstrap()
                    .group(EventLoopGroupFactory.getBossGroup(), EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(this);
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

    // Método auxiliar para inyectar dependencias en objetos creados manualmente.
    private <T> T injectMembers(T object) {
        beanFactory.autowireBean(object);
        return object;
    }

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        // 1. Agregar handlers de transporte
        addTransportHandlers(pipeline::addLast);

        // 2. Agregar IdleStateHandler si se usa TCP y se configuró un timeout
        if (timeout > 0 && !connector.isDatagram()) {
            pipeline.addLast(new IdleStateHandler(timeout, 0, 0));
        }

        // 3. Registrar la apertura del canal
        pipeline.addLast(new OpenChannelHandler(connector));

        // 4. Agregar NetworkForwarderHandler si el forwarding está habilitado
        if (serverForward) {
            pipeline.addLast(injectMembers(new NetworkForwarderHandler(protocolPort)));
        }

        // 5. Agregar NetworkMessageHandler para transformar mensajes a NetworkMessage
        pipeline.addLast(new NetworkMessageHandler());

        // 6. Agregar StandardLoggingHandler para depuración (log de mensajes)
        pipeline.addLast(injectMembers(new StandardLoggingHandler(protocol)));

        // 7. Agregar AcknowledgementHandler si se usa TCP y no se habilita el ACK instantáneo
        if (!connector.isDatagram() && !serverInstantAck) {
            pipeline.addLast(new AcknowledgementHandler());
        }

        // 8. Agregar handlers específicos del protocolo (decoders/encoders)
        addProtocolHandlers(handler -> {
            if (handler instanceof BaseProtocolDecoder || handler instanceof BaseProtocolEncoder) {
                injectMembers(handler);
            } else {
                if (handler instanceof ChannelInboundHandler) {
                    handler = new WrapperInboundHandler((ChannelInboundHandler) handler);
                } else if (handler instanceof ChannelOutboundHandler) {
                    handler = new WrapperOutboundHandler((ChannelOutboundHandler) handler);
                }
            }
            pipeline.addLast(handler);
        });

        // 9. Agregar handlers finales de procesamiento y direccionamiento, obtenidos vía Spring
        pipeline.addLast(beanFactory.getBean(RemoteAddressHandler.class));
        pipeline.addLast(beanFactory.getBean(ProcessingHandler.class));
        pipeline.addLast(beanFactory.getBean(MainEventHandler.class));
    }

    /**
     * Interfaz funcional para facilitar la adición de handlers al pipeline.
     */
    @FunctionalInterface
    public interface PipelineBuilder {
        void addLast(ChannelHandler handler);
    }

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
}