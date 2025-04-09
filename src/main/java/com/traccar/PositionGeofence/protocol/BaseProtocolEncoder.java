package com.traccar.PositionGeofence.protocol;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.helper.NetworkUtil;
import com.traccar.PositionGeofence.helper.model.AttributeUtil;
import com.traccar.PositionGeofence.modelo.Command;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.session.cache.CacheManager;



@Component
public abstract class BaseProtocolEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProtocolEncoder.class);

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Protocol protocol;
    private CacheManager cacheManager;
    private String modelOverride;

    public BaseProtocolEncoder(Protocol protocol) {
        this.protocol = protocol;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Retorna el nombre del protocolo o "unknown" si no hay protocolo definido.
     */
    public String getProtocolName() {
        return protocol != null ? protocol.getName() : PROTOCOL_UNKNOWN;
    }

    /**
     * Obtiene el uniqueId del dispositivo a partir del cacheManager.
     */
    protected String getUniqueId(long deviceId) {
        Device device = cacheManager.getObject(Device.class, deviceId);
        return device != null ? device.getUniqueId() : null;
    }

    /**
     * Inicializa la contraseña del dispositivo en el comando si aún no se ha establecido.
     */
    protected void initDevicePassword(Command command, String defaultPassword) {
        if (!command.hasAttribute(Command.KEY_DEVICE_PASSWORD)) {
            String password = AttributeUtil.getDevicePassword(
                    cacheManager, command.getDeviceId(), getProtocolName(), defaultPassword);
            command.set(Command.KEY_DEVICE_PASSWORD, password);
        }
    }

    /**
     * Permite sobreescribir el modelo del dispositivo.
     */
    public void setModelOverride(String modelOverride) {
        this.modelOverride = modelOverride;
    }

    /**
     * Obtiene el modelo del dispositivo; si se ha definido un modelOverride, se utiliza ese.
     */
    // public String getDeviceModel(long deviceId) {
    //     Device device = getCacheManager().getObject(Device.class, deviceId);
    //     String model = device != null ? device.getModel() : null;
    //     return modelOverride != null ? modelOverride : model;
    // }

    /**
     * Intercepta la escritura en el canal. Si el mensaje es un NetworkMessage que contiene un Command,
     * codifica el comando y lo escribe en el canal, registrando un log informativo.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof NetworkMessage networkMessage) {
            Object innerMsg = networkMessage.getMessage();
            if (innerMsg instanceof Command command) {
                Object encodedCommand = encodeCommand(ctx.channel(), command);

                StringBuilder s = new StringBuilder();
                s.append("[").append(NetworkUtil.session(ctx.channel())).append("] ");
                s.append("id: ").append(getUniqueId(command.getDeviceId())).append(", ");
                s.append("command type: ").append(command.getType()).append(" ");
                s.append(encodedCommand != null ? "sent" : "not sent");
                LOGGER.info(s.toString());

                ctx.write(new NetworkMessage(encodedCommand, networkMessage.getRemoteAddress()), promise);
                return;
            }
        }
        super.write(ctx, msg, promise);
    }

    /**
     * Permite codificar un comando basándose en el canal. Se delega a encodeCommand(Command).
     */
    protected Object encodeCommand(Channel channel, Command command) {
        return encodeCommand(command);
    }

    /**
     * Método para codificar un comando; por defecto, devuelve null. Se espera que las subclases
     * implementen la lógica de codificación para el protocolo específico.
     */
    protected Object encodeCommand(Command command) {
        return null;
    }
}