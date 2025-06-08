package com.traccar.PositionGeofence;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.helper.UnitsConverter;
import com.traccar.PositionGeofence.helper.model.AttributeUtil;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.protocol.ExtendedObjectDecoder;
import com.traccar.PositionGeofence.session.ConnectionManager;
import com.traccar.PositionGeofence.session.DeviceSession;
import com.traccar.PositionGeofence.session.cache.CacheManager;
import com.traccar.PositionGeofence.database.MediaManager;
//import com.traccar.PositionGeofence.database.StatisticsManager;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseProtocolDecoder extends ExtendedObjectDecoder {

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Protocol protocol;

    // Dependencias inyectadas
    private CacheManager cacheManager;
    private ConnectionManager connectionManager;
    //private StatisticsManager statisticsManager;
    private MediaManager mediaManager;
    //private CommandsManager commandsManager;

    // Valor opcional para forzar un modelo de dispositivo específico
    private String modelOverride;

    public BaseProtocolDecoder(Protocol protocol) {
        this.protocol = protocol;
    }

    // ---- Inyección de dependencias ----
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    @Autowired
    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    @Autowired
    public void setMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    
    // ---- Métodos de utilidad ----
    public String writeMediaFile(String uniqueId, ByteBuf buf, String extension) {
        return mediaManager.writeFile(uniqueId, buf, extension);
    }
    
    public String getProtocolName() {
        return protocol != null ? protocol.getName() : PROTOCOL_UNKNOWN;
    }
    
    /**
     * Retorna la dirección del servidor. Primero consulta la configuración; si no hay,
     * utiliza la dirección local del canal.
     */
    public String getServer(Channel channel, char delimiter) {
        String server = getConfig().getString(Keys.PROTOCOL_SERVER.withPrefix(getProtocolName()));
        if (server == null && channel != null) {
            InetSocketAddress address = (InetSocketAddress) channel.localAddress();
            server = address.getAddress().getHostAddress() + ":" + address.getPort();
        }
        return server != null ? server.replace(':', delimiter) : null;
    }
    
    protected double convertSpeed(double value, String defaultUnits) {
        // Se consulta la configuración del protocolo para conocer la unidad utilizada y se convierte a nudos.
        String units = getConfig().getString(getProtocolName() + ".speed", defaultUnits);
        return switch (units) {
            case "kmh" -> UnitsConverter.knotsFromKph(value);
            case "mps" -> UnitsConverter.knotsFromMps(value);
            case "mph" -> UnitsConverter.knotsFromMph(value);
            default -> value;
        };
    }

    protected TimeZone getTimeZone(long deviceId) {
        return getTimeZone(deviceId, "UTC");
    }

    protected TimeZone getTimeZone(long deviceId, String defaultTimeZone) {
        String timeZoneName = AttributeUtil.lookup(cacheManager, Keys.DECODER_TIMEZONE, deviceId);
        if (timeZoneName != null) {
            return TimeZone.getTimeZone(timeZoneName);
        } else if (defaultTimeZone != null) {
            return TimeZone.getTimeZone(defaultTimeZone);
        }
        return null;
    }
    
    /**
     * Obtiene o crea la sesión del dispositivo a partir de la conexión.
     */
    public DeviceSession getDeviceSession(Channel channel, SocketAddress remoteAddress, String... uniqueIds) {
        try {
            return connectionManager.getDeviceSession(protocol, channel, remoteAddress, uniqueIds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setModelOverride(String modelOverride) {
        this.modelOverride = modelOverride;
    }
    
    public String getDeviceModel(DeviceSession deviceSession) {
        return modelOverride != null ? modelOverride : deviceSession.getModel();
    }
    
    /**
     * Marca la posición como desactualizada y asigna la hora del dispositivo.
     */
    public void getLastLocation(Position position, Date deviceTime) {
        if (position.getDeviceId() != 0) {
            position.setOutdated(true);
            if (deviceTime != null) {
                position.setDeviceTime(deviceTime);
            }
        }
    }
    

    
    /**
     * Maneja el caso de un mensaje "vacío" (por ejemplo, heartbeat).
     * Si la configuración lo permite, crea un objeto Position con datos mínimos.
     */
    @Override
    protected Object handleEmptyMessage(Channel channel, SocketAddress remoteAddress, Object msg) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (getConfig().getBoolean(Keys.DATABASE_SAVE_EMPTY) && deviceSession != null) {
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            getLastLocation(position, null);
            return position;
        } else {
            return null;
        }
    }
    
   
}