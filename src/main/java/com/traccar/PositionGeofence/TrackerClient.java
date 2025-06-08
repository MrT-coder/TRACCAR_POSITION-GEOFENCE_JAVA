package com.traccar.PositionGeofence;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.springframework.context.ApplicationContext;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.protocol.BasePipelineFactory;
import com.traccar.PositionGeofence.protocol.EventLoopGroupFactory;
import com.traccar.PositionGeofence.protocol.PipelineBuilder;

import java.util.concurrent.TimeUnit;

public abstract class TrackerClient implements TrackerConnector {

    private final boolean secure;
    private final long interval;

    private final Bootstrap bootstrap;

    private final int port;
    private final String address;
    private final String[] devices;

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public boolean isDatagram() {
        return false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    public TrackerClient(Config config, String protocol) {
        secure = config.getBoolean(Keys.PROTOCOL_SSL.withPrefix(protocol));
        interval = config.getLong(Keys.PROTOCOL_INTERVAL.withPrefix(protocol));
        address = config.getString(Keys.PROTOCOL_ADDRESS.withPrefix(protocol));
        port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocol), secure ? 443 : 80);
        devices = config.getString(Keys.PROTOCOL_DEVICES.withPrefix(protocol)).split("[, ]");

        BasePipelineFactory pipelineFactory = new BasePipelineFactory(this, config, protocol) {
            @Override
            protected void addTransportHandlers(PipelineBuilder pipeline) {
                try {
                    if (isSecure()) {
                        SSLEngine engine = SSLContext.getDefault().createSSLEngine();
                        engine.setUseClientMode(true);
                        pipeline.addLast(new SslHandler(engine));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline) {
                try {
                    TrackerClient.this.addProtocolHandlers(pipeline, config);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setApplicationContext(ApplicationContext applicationContext) {
                // Implementation for setting the application context
            }
        };

        bootstrap = new Bootstrap()
                .group(EventLoopGroupFactory.getWorkerGroup())
                .channel(NioSocketChannel.class)
                .handler(pipelineFactory);
    }

    protected abstract void addProtocolHandlers(PipelineBuilder pipeline, Config config) throws Exception;

    public String[] getDevices() {
        return devices;
    }

    @Override
    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    @Override
    public void start() throws Exception {
        bootstrap.connect(address, port)
                .syncUninterruptibly().channel().closeFuture().addListener(new GenericFutureListener<>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) {
                        if (interval > 0) {
                            GlobalEventExecutor.INSTANCE.schedule(() -> {
                                bootstrap.connect(address, port)
                                        .syncUninterruptibly().channel().closeFuture().addListener(this);
                            }, interval, TimeUnit.SECONDS);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        channelGroup.close().awaitUninterruptibly();
    }

}