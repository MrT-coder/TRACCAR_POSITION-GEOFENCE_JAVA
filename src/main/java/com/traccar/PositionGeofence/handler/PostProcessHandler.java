package com.traccar.PositionGeofence.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.traccar.PositionGeofence.client.DeviceClient;
import com.traccar.PositionGeofence.helper.model.PositionUtil;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.session.ConnectionManager;
import com.traccar.PositionGeofence.session.cache.CacheManager;

@Component
public class PostProcessHandler extends BasePositionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessHandler.class);

    private final CacheManager cacheManager;
    private final ConnectionManager connectionManager;
    private final DeviceClient deviceClient;

    @Autowired
    public PostProcessHandler(CacheManager cacheManager,
            ConnectionManager connectionManager,
            DeviceClient deviceClient) {
        this.cacheManager = cacheManager;
        this.connectionManager = connectionManager;
        this.deviceClient = deviceClient;
    }

    @Override
    public void onPosition(Position position, Callback callback) {
        try {
            // Obtén la última posición conocida para el dispositivo.
            Position lastPosition = cacheManager.getPosition(position.getDeviceId());
            // Verifica si la posición es la más reciente para el dispositivo (utilizando
            // PositionUtil)
            if (PositionUtil.isLatest(lastPosition, position)) {
                // Construye un objeto Device con el nuevo positionId.
                Device updatedDevice = new Device();
                updatedDevice.setId(position.getDeviceId());
                updatedDevice.setPositionId(position.getId());

                // Delegar la actualización del Device al microservicio Device mediante REST.
                deviceClient.updateDevice(updatedDevice);

                // Actualiza la posición en la caché.
                cacheManager.updatePosition(position);

                // Notifica al ConnectionManager para actualizar el estado del dispositivo.
                connectionManager.updatePosition(true, position);
            }
        } catch (Exception ex) {
            LOGGER.warn("Error updating device via DeviceClient", ex);
        }
        // Indica que el procesamiento de la posición ha finalizado (el parámetro false
        // indica que la posición no fue filtrada)
        callback.processed(false);
    }
}