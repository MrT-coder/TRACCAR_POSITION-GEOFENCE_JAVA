package com.traccar.PositionGeofence.protocol;

import com.traccar.PositionGeofence.BaseProtocolDecoder;
import com.traccar.PositionGeofence.Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public abstract class BaseHttpProtocolDecoder extends BaseProtocolDecoder {

    public BaseHttpProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    public void sendResponse(Channel channel, HttpResponseStatus status) {
        sendResponse(channel, status, null);
    }

    public void sendResponse(Channel channel, HttpResponseStatus status, ByteBuf buf) {
        if (channel != null) {
            if (buf == null) {
                buf = Unpooled.buffer(0);
            }
            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
            response.headers().add(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
            channel.writeAndFlush(new NetworkMessage(response, channel.remoteAddress()));
        }
    }

}