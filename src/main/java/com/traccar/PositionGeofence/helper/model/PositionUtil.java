package com.traccar.PositionGeofence.helper.model;

import com.traccar.PositionGeofence.client.DeviceClient;
import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.storage.QueryRequest;
import com.traccar.PositionGeofence.storage.Storage;
import com.traccar.PositionGeofence.storage.StorageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

public final class PositionUtil {

    private PositionUtil() {
    }

    /**
     * Determina si la posición es la más reciente para un dispositivo.
     * Compara el campo fixTime con la última posición registrada en el CacheManager.
     */
    public static boolean isLatest(Position position, Position lastPosition) {
        return lastPosition == null || position.getFixTime().compareTo(lastPosition.getFixTime()) >= 0;
    }

    /**
     * Calcula la distancia entre dos posiciones.
     * Usa valores de odómetro si se indica, o la diferencia en total distance.
     */
    public static double calculateDistance(Position first, Position last, boolean useOdometer) {
        double firstOdometer = first.getDouble(Position.KEY_ODOMETER);
        double lastOdometer = last.getDouble(Position.KEY_ODOMETER);
        if (useOdometer && firstOdometer != 0.0 && lastOdometer != 0.0) {
            return lastOdometer - firstOdometer;
        } else {
            return last.getDouble(Position.KEY_TOTAL_DISTANCE) - first.getDouble(Position.KEY_TOTAL_DISTANCE);
        }
    }

    /**
     * Recupera las posiciones de un dispositivo entre dos fechas.
     * Usa la API de Spring Data MongoDB para construir la consulta.
     */
    public static List<Position> getPositions(Storage storage, long deviceId, Date from, Date to) throws StorageException {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("deviceId").is(deviceId)
                        .and("fixTime").gte(from).lte(to)
        );
        return storage.getObjects(Position.class, new QueryRequest(query));
    }

    /**
     * Obtiene las últimas posiciones para cada dispositivo asociado a un usuario.
     * En vez de usar Storage para obtener dispositivos (como en el monolito SQL), se consulta a DeviceClient.
     * @throws Exception 
     */
    public static List<Position> getLatestPositions(Storage storage, long userId, DeviceClient deviceClient) throws Exception {
        // Obtener los dispositivos asociados al usuario a través de REST
        List<Device> devices = deviceClient.getDevicesByUser(userId);
        Set<Long> deviceIds = devices.stream().map(BaseModel::getId).collect(Collectors.toSet());

        // Consultar todas las posiciones de esos dispositivos
        Query query = new Query();
        query.addCriteria(Criteria.where("deviceId").in(deviceIds));
        query.with(Sort.by(Sort.Direction.DESC, "fixTime"));
        List<Position> positions = storage.getObjects(Position.class, new QueryRequest(query));

        // Seleccionar la posición más reciente para cada dispositivo
        Map<Long, Position> latestPositions = positions.stream()
                .collect(Collectors.toMap(
                        Position::getDeviceId,
                        pos -> pos,
                        (pos1, pos2) -> pos1.getFixTime().after(pos2.getFixTime()) ? pos1 : pos2
                ));
        return new ArrayList<>(latestPositions.values());
    }
}
