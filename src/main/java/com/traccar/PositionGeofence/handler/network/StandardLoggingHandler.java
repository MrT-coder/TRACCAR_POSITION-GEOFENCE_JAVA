package com.traccar.PositionGeofence.handler.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.NetworkMessage;
import com.traccar.PositionGeofence.helper.BufferUtil;
import com.traccar.PositionGeofence.helper.NetworkUtil;
import com.traccar.PositionGeofence.modelo.LogRecord;

import java.nio.charset.StandardCharsets;

@Component
public class StandardLoggingHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardLoggingHandler.class);

    // El nombre del protocolo, se inyecta mediante @Value (configurable en application.properties)
    private final String protocol;

    // Determina si se decodifica el contenido textual del mensaje
    private boolean decodeTextData;

    // Se puede inyectar el protocol mediante constructor, 
    // o, si lo prefieres, también se puede inyectar con @Value directamente.
    public StandardLoggingHandler(@Value("${logger.protocol.name}") String protocol) {
        this.protocol = protocol;
    }

    // Inyección de la propiedad para determinar si se decodifica texto (por defecto false)
    @Value("${logger.text.protocol:false}")
    public void setDecodeTextData(boolean decodeTextData) {
        this.decodeTextData = decodeTextData;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LogRecord record = createLogRecord(ctx, msg);
        log(ctx, false, record);
        super.channelRead(ctx, msg);
        // En esta versión, se elimina la llamada a connectionManager.updateLog(record)
        // ya que el log detallado se delega a sistemas de monitoreo externos.
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log(ctx, true, createLogRecord(ctx, msg));
        super.write(ctx, msg, promise);
    }

    private LogRecord createLogRecord(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof NetworkMessage networkMessage) {
            if (networkMessage.getMessage() instanceof ByteBuf data) {
                LogRecord record = new LogRecord(ctx.channel().localAddress(), networkMessage.getRemoteAddress());
                record.setProtocol(protocol);
                if (decodeTextData && BufferUtil.isPrintable(data, data.readableBytes())) {
                    record.setData(data.getCharSequence(
                            data.readerIndex(), data.readableBytes(), StandardCharsets.US_ASCII).toString()
                            .replace("\r", "\\r").replace("\n", "\\n"));
                } else {
                    record.setData(ByteBufUtil.hexDump(data));
                }
                return record;
            }
        }
        return null;
    }

    private void log(ChannelHandlerContext ctx, boolean downstream, LogRecord record) {
        if (record != null) {
            StringBuilder message = new StringBuilder();
            message.append("[").append(NetworkUtil.session(ctx.channel())).append(": ");
            message.append(protocol);
            message.append(downstream ? " > " : " < ");
            message.append(record.getAddress().getHostString());
            message.append("] ");
            message.append(record.getData());
            LOGGER.info(message.toString());
        }
    }
}