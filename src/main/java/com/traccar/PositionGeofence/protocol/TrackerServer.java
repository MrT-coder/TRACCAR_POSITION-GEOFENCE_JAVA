package com.traccar.PositionGeofence.protocol;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.net.InetSocketAddress;

@Component
public abstract class TrackerServer implements TrackerConnector {

    private final boolean datagram;
    private final boolean secure;
    private final AbstractBootstrap<?, ?> bootstrap;
    private final int port;
    private final String address;
    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // Usamos @Value para inyectar las propiedades desde application.properties
    public TrackerServer(
            @Value("${protocol.ssl:false}") boolean secure,
            @Value("${protocol.address:0.0.0.0}") String address,
            @Value("${protocol.port:5050}") int port,
            @Value("${protocol.datagram:false}") boolean datagram) {

        this.secure = secure;
        this.address = address;
        this.port = port;
        this.datagram = datagram;

        // Para el MVP, usamos un BasePipelineFactory mínimo. Aquí se inyecta un protocolo por defecto ("default")
        // y un timeout fijo (60 segundos); estos valores se podrán mejorar o parametrizar.
        BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, secure) {
            @Override
            protected void addTransportHandlers(PipelineBuilder pipeline) {
                try {
                    if (secure) {
                        // Se crea el SslHandler con el contexto por defecto
                        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
                        pipeline.addLast(new SslHandler(engine));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                TrackerServer.this.addProtocolHandlers(pipeline);
            }
        };

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

    // Método abstracto para que las subclases agreguen los handlers específicos del protocolo
    protected abstract void addProtocolHandlers(PipelineBuilder pipeline);

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

    @Override
    public boolean isDatagram() {
        return datagram;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

}