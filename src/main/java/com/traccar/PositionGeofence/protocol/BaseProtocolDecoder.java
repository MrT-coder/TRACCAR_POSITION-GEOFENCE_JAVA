package com.traccar.PositionGeofence.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.database.CommandsManager;
import com.traccar.PositionGeofence.database.MediaManager;
import com.traccar.PositionGeofence.database.StatisticsManager;
import com.traccar.PositionGeofence.helper.UnitsConverter;
import com.traccar.PositionGeofence.helper.model.AttributeUtil;
import com.traccar.PositionGeofence.modelo.Command;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.session.ConnectionManager;
import com.traccar.PositionGeofence.session.DeviceSession;
import com.traccar.PositionGeofence.session.cache.CacheManager;

// Anotamos con @Component para que se gestione como bean en Spring
@Component
public abstract class BaseProtocolDecoder extends ExtendedObjectDecoder {

    private static final String PROTOCOL_UNKNOWN = "unknown";

    private final Protocol protocol;

    private CacheManager cacheManager;
    private ConnectionManager connectionManager;
    private StatisticsManager statisticsManager;
    private MediaManager mediaManager;
    private CommandsManager commandsManager;

    private String modelOverride;

    // Inyectamos el Environment para acceder a propiedades
    @Autowired
    private Environment env;

    public BaseProtocolDecoder(Protocol protocol) {
        this.protocol = protocol;
    }

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
    public void setStatisticsManager(StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }

    @Autowired
    public void setMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @Autowired
    public void setCommandsManager(CommandsManager commandsManager) {
        this.commandsManager = commandsManager;
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    public String writeMediaFile(String uniqueId, ByteBuf buf, String extension) {
        return mediaManager.writeFile(uniqueId, buf, extension);
    }

    public String getProtocolName() {
        return protocol != null ? protocol.getName() : PROTOCOL_UNKNOWN;
    }

    public String getServer(Channel channel, char delimiter) {
        // Se espera que la propiedad se defina como "protocol.server.<nombreDelProtocolo>"
        String key = "protocol.server." + getProtocolName();
        String server = env.getProperty(key);
        if (server == null && channel != null) {
            InetSocketAddress address = (InetSocketAddress) channel.localAddress();
            server = address.getAddress().getHostAddress() + ":" + address.getPort();
        }
        return server != null ? server.replace(':', delimiter) : null;
    }

    protected double convertSpeed(double value, String defaultUnits) {
        // La propiedad se define como "protocol.speed.<nombreDelProtocolo>"
        String key = "protocol.speed." + getProtocolName();
        String unit = env.getProperty(key, defaultUnits);
        return switch (unit) {
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
        // Se utiliza la propiedad "decoder.timezone" en lugar de Keys.DECODER_TIMEZONE
        String timeZoneName = AttributeUtil.lookup(cacheManager, "decoder.timezone", deviceId);
        if (timeZoneName != null) {
            return TimeZone.getTimeZone(timeZoneName);
        } else if (defaultTimeZone != null) {
            return TimeZone.getTimeZone(defaultTimeZone);
        }
        return null;
    }

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

    public void getLastLocation(Position position, Date deviceTime) {
        if (position.getDeviceId() != 0) {
            position.setOutdated(true);
            if (deviceTime != null) {
                position.setDeviceTime(deviceTime);
            }
        }
    }

    @Override
    protected void onMessageEvent(Channel channel, SocketAddress remoteAddress, Object originalMessage, Object decodedMessage) {
        if (statisticsManager != null) {
            statisticsManager.registerMessageReceived();
        }
        Set<Long> deviceIds = new HashSet<>();
        if (decodedMessage != null) {
            if (decodedMessage instanceof Position position) {
                deviceIds.add(position.getDeviceId());
            } else if (decodedMessage instanceof Collection) {
                Collection<Position> positions = (Collection) decodedMessage;
                for (Position position : positions) {
                    deviceIds.add(position.getDeviceId());
                }
            }
        }
        if (deviceIds.isEmpty()) {
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession != null) {
                deviceIds.add(deviceSession.getDeviceId());
            }
        }
        for (long deviceId : deviceIds) {
            connectionManager.updateDevice(deviceId, Device.STATUS_ONLINE, new Date());
            sendQueuedCommands(channel, remoteAddress, deviceId);
        }
    }

    protected void sendQueuedCommands(Channel channel, SocketAddress remoteAddress, long deviceId) {
        for (Command command : commandsManager.readQueuedCommands(deviceId)) {
            protocol.sendDataCommand(channel, remoteAddress, command);
        }
    }

    @Override
    protected Object handleEmptyMessage(Channel channel, SocketAddress remoteAddress, Object msg) {
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        // Se usa la propiedad "database.saveEmpty" en lugar de Keys.DATABASE_SAVE_EMPTY
        if (Boolean.parseBoolean(env.getProperty("database.saveEmpty", "false")) && deviceSession != null) {
            Position position = new Position(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            getLastLocation(position, null);
            return position;
        } else {
            return null;
        }
    }
}