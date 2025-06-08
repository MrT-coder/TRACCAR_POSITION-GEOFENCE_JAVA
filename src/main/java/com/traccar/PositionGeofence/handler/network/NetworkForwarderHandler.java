package com.traccar.PositionGeofence.handler.network;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import org.springframework.beans.factory.annotation.Autowired;

import com.traccar.PositionGeofence.forward.NetworkForwarder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class NetworkForwarderHandler extends ChannelInboundHandlerAdapter {

    private final int port;
    private NetworkForwarder networkForwarder;

    public NetworkForwarderHandler(int port) {
        this.port = port;
    }

    @Autowired
    public void setNetworkForwarder(NetworkForwarder networkForwarder) {
        this.networkForwarder = networkForwarder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean datagram = ctx.channel() instanceof DatagramChannel;
        SocketAddress remoteAddress;
        ByteBuf buffer;
        if (datagram) {
            DatagramPacket message = (DatagramPacket) msg;
            remoteAddress = message.recipient();
            buffer = message.content();
        } else {
            remoteAddress = ctx.channel().remoteAddress();
            buffer = (ByteBuf) msg;
        }
        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        networkForwarder.forward((InetSocketAddress) remoteAddress, port, datagram, data);
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!(ctx.channel() instanceof DatagramChannel)) {
            networkForwarder.disconnect((InetSocketAddress) ctx.channel().remoteAddress());
        }
        super.channelInactive(ctx);
    }
}