package com.traccar.PositionGeofence.database;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.modelo.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

@Component
public class BufferingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferingManager.class);

    public interface Callback {
        void onReleased(ChannelHandlerContext context, Position position);
    }

    private static final class Holder implements Comparable<Holder> {

        private final ChannelHandlerContext context;
        private final Position position;
        private Timeout timeout;

        private Holder(ChannelHandlerContext context, Position position) {
            this.context = context;
            this.position = position;
        }

        private int compareTime(java.util.Date left, java.util.Date right) {
            if (left != null && right != null) {
                return left.compareTo(right);
            }
            return 0;
        }

        @Override
        public int compareTo(Holder other) {
            int fixTimeResult = compareTime(position.getFixTime(), other.position.getFixTime());
            if (fixTimeResult != 0) {
                return fixTimeResult;
            }
            int deviceTimeResult = compareTime(position.getDeviceTime(), other.position.getDeviceTime());
            if (deviceTimeResult != 0) {
                return deviceTimeResult;
            }
            return position.getServerTime().compareTo(other.position.getServerTime());
        }
    }

    private final Timer timer = new HashedWheelTimer();
    private final Callback callback;
    private final long threshold;
    // Buffer: para cada dispositivo, un conjunto ordenado de posiciones.
    private final Map<Long, TreeSet<Holder>> buffer = new HashMap<>();

    /**
     * Se espera que en la configuración de Spring Boot tengas la clave 'server.buffering.threshold'
     * configurada en el application.yml o properties.
     */
    public BufferingManager(Config config, Callback callback) {
        this.callback = callback;
        this.threshold = config.getLong(Keys.SERVER_BUFFERING_THRESHOLD);
    }

    private Timeout scheduleTimeout(Holder holder) {
        return timer.newTimeout(timeout -> {
            LOGGER.info("Released position fixTime: {}", holder.position.getFixTime());
            synchronized (buffer) {
                TreeSet<Holder> queue = buffer.get(holder.position.getDeviceId());
                if(queue != null) {
                    queue.remove(holder);
                }
            }
            callback.onReleased(holder.context, holder.position);
        }, threshold, TimeUnit.MILLISECONDS);
    }

    public void accept(ChannelHandlerContext context, Position position) {
        if (threshold > 0) {
            synchronized (buffer) {
                LOGGER.info("Queued position fixTime: {}", position.getFixTime());
                var queue = buffer.computeIfAbsent(position.getDeviceId(), k -> new TreeSet<>());
                Holder holder = new Holder(context, position);
                holder.timeout = scheduleTimeout(holder);
                queue.add(holder);
                // Para asegurar la liberación en orden, se reprograman los timeout
                queue.tailSet(holder).forEach(h -> {
                    h.timeout.cancel();
                    h.timeout = scheduleTimeout(h);
                });
            }
        } else {
            // Si no se establece threshold, se libera la posición inmediatamente
            callback.onReleased(context, position);
        }
    }
}