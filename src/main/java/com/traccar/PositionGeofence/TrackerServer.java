package com.traccar.PositionGeofence;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.protocol.BasePipelineFactory;
import com.traccar.PositionGeofence.protocol.EventLoopGroupFactory;
import com.traccar.PositionGeofence.protocol.PipelineBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

@Component
public abstract class TrackerServer implements TrackerConnector {

    private final boolean datagram;
    private final boolean secure;
    @SuppressWarnings("rawtypes")
    private final AbstractBootstrap bootstrap;
    private final int port;
    private final String address;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public boolean isDatagram() {
        return datagram;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }


    public TrackerServer(Config config, String protocol, boolean datagram) {
        secure = config.getBoolean(Keys.PROTOCOL_SSL.withPrefix(protocol));
        address = config.getString(Keys.PROTOCOL_ADDRESS.withPrefix(protocol));
        port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocol));

        BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, config, protocol) {
            @Override
            protected void addTransportHandlers(PipelineBuilder pipeline) {
                try {
                    if (isSecure()) {
                        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
                        pipeline.addLast(new SslHandler(engine));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                TrackerServer.this.addProtocolHandlers(pipeline, config);
            }
        };

        this.datagram = datagram;
        if (datagram) {
            bootstrap = new Bootstrap()
                    .group(EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioDatagramChannel.class)
                    .handler(pipelineFactory);
        } else {
            bootstrap = new ServerBootstrap()
                    .group(EventLoopGroupFactory.getBossGroup(), EventLoopGroupFactory.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(pipelineFactory);
        }
    }

    // Método abstracto para que las subclases agreguen los handlers específicos del
    // protocolo
    protected abstract void addProtocolHandlers(PipelineBuilder pipeline, Config config);

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    /**
     * Inicia el servidor al arrancar el bean.
     */
    @PostConstruct
    @Override
    public void start() throws Exception {
        InetSocketAddress endpoint;
        if (address == null || address.isEmpty()) {
            endpoint = new InetSocketAddress(port);
        } else {
            endpoint = new InetSocketAddress(address, port);
        }
        Channel channel = bootstrap.bind(endpoint).syncUninterruptibly().channel();
        if (channel != null) {
            getChannelGroup().add(channel);
        }
    }

    /**
     * Detiene el servidor antes de destruir el bean.
     */
    @PreDestroy
    @Override
    public void stop() {
        channelGroup.close().awaitUninterruptibly();
    }

    
}