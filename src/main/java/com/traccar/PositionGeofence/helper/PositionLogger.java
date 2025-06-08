package com.traccar.PositionGeofence.helper;

import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.session.cache.CacheManager;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class PositionLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionLogger.class);

    private final CacheManager cacheManager;

    // Cadena de atributos que se quieren loguear, configurados en application.properties
    // Ejemplo en application.properties:
    // logger.attributes=time,position,speed,course,accuracy,outdated,invalid
    @Value("${logger.attributes}")
    private String loggerAttributes;

    private Set<String> logAttributes = new LinkedHashSet<>();

    public PositionLogger(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        // Separamos la cadena usando coma o espacios y llenamos el conjunto de atributos
        if (loggerAttributes != null && !loggerAttributes.trim().isEmpty()) {
            logAttributes.addAll(Arrays.asList(loggerAttributes.split("[, ]+")));
        }
    }

    /**
     * Loguea la posición, mostrando la información especificada en loggerAttributes.
     * 
     * @param context El contexto del canal (se utiliza para obtener información de la sesión).
     * @param position La posición a loguear.
     */
    public void log(ChannelHandlerContext context, Position position) {
        // Obtenemos el dispositivo a partir del cacheManager
        Device device = cacheManager.getObject(Device.class, position.getDeviceId());
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(NetworkUtil.session(context.channel())).append("] ");
        builder.append("id: ").append(device.getUniqueId());
        
        for (String attribute : logAttributes) {
            switch (attribute) {
                case "time":
                    builder.append(", time: ").append(DateUtil.formatDate(position.getFixTime(), false));
                    break;
                case "position":
                    builder.append(", lat: ").append(String.format("%.5f", position.getLatitude()));
                    builder.append(", lon: ").append(String.format("%.5f", position.getLongitude()));
                    break;
                case "speed":
                    if (position.getSpeed() > 0) {
                        builder.append(", speed: ").append(String.format("%.1f", position.getSpeed()));
                    }
                    break;
                case "course":
                    builder.append(", course: ").append(String.format("%.1f", position.getCourse()));
                    break;
                case "accuracy":
                    if (position.getAccuracy() > 0) {
                        builder.append(", accuracy: ").append(String.format("%.1f", position.getAccuracy()));
                    }
                    break;
                case "outdated":
                    if (position.getOutdated()) {
                        builder.append(", outdated");
                    }
                    break;
                case "invalid":
                    if (!position.getValid()) {
                        builder.append(", invalid");
                    }
                    break;
                default:
                    Object value = position.getAttributes().get(attribute);
                    if (value != null) {
                        builder.append(", ").append(attribute).append(": ").append(value);
                    }
                    break;
            }
        }
        LOGGER.info(builder.toString());
    }
}