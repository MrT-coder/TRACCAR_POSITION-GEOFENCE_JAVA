package com.traccar.PositionGeofence.protocol;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.traccar.PositionGeofence.Protocol;
import com.traccar.PositionGeofence.helper.DateUtil;
import com.traccar.PositionGeofence.modelo.CellTower;
import com.traccar.PositionGeofence.modelo.Command;
import com.traccar.PositionGeofence.modelo.Network;
import com.traccar.PositionGeofence.modelo.Position;
import com.traccar.PositionGeofence.modelo.WifiAccessPoint;
import com.traccar.PositionGeofence.session.DeviceSession;

public class OsmAndProtocolDecoder extends BaseHttpProtocolDecoder {

    public OsmAndProtocolDecoder(Protocol protocol) {
        super(protocol);
    }

    @Override
    public Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        FullHttpRequest request = (FullHttpRequest) msg;
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            decoder = new QueryStringDecoder(request.content().toString(StandardCharsets.US_ASCII), false);
            params = decoder.parameters();
        }

        // 1) Validación de parámetro id | deviceid
        List<String> ids = params.get("id");
        if (ids == null) {
            ids = params.get("deviceid");
        }
        if (ids == null || ids.isEmpty()) {
            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
            return null;
        }
       
        // 3) Creamos la Position y asignamos deviceId
        Position position = new Position(getProtocolName());
        position.setValid(true);

        Network network = new Network();
        Double latitude = null;
        Double longitude = null;

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                switch (entry.getKey()) {
                    case "id":
                    case "deviceid":
                        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, value);
                        if (deviceSession == null) {
                            sendResponse(channel, HttpResponseStatus.BAD_REQUEST);
                            return null;
                        }
                        position.setDeviceId(deviceSession.getDeviceId());
                        break;
                    case "valid":
                        position.setValid(Boolean.parseBoolean(value) || "1".equals(value));
                        break;
                    case "timestamp":
                        try {
                            long timestamp = Long.parseLong(value);
                            if (timestamp < Integer.MAX_VALUE) {
                                timestamp *= 1000;
                            }
                            position.setTime(new Date(timestamp));
                        } catch (NumberFormatException error) {
                            if (value.contains("T")) {
                                position.setTime(DateUtil.parseDate(value));
                            } else {
                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                position.setTime(dateFormat.parse(value));
                            }
                        }
                        break;
                    case "lat":
                        latitude = Double.parseDouble(value);
                        break;
                    case "lon":
                        longitude = Double.parseDouble(value);
                        break;
                    case "location":
                        String[] location = value.split(",");
                        latitude = Double.parseDouble(location[0]);
                        longitude = Double.parseDouble(location[1]);
                        break;
                    case "cell":
                        String[] cell = value.split(",");
                        if (cell.length > 4) {
                            network.addCellTower(CellTower.from(
                                    Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                    Integer.parseInt(cell[2]), Integer.parseInt(cell[3]), Integer.parseInt(cell[4])));
                        } else {
                            network.addCellTower(CellTower.from(
                                    Integer.parseInt(cell[0]), Integer.parseInt(cell[1]),
                                    Integer.parseInt(cell[2]), Integer.parseInt(cell[3])));
                        }
                        break;
                    case "wifi":
                        String[] wifi = value.split(",");
                        network.addWifiAccessPoint(WifiAccessPoint.from(
                                wifi[0].replace('-', ':'), Integer.parseInt(wifi[1])));
                        break;
                    case "speed":
                        position.setSpeed(convertSpeed(Double.parseDouble(value), "kn"));
                        break;
                    case "bearing":
                    case "heading":
                        position.setCourse(Double.parseDouble(value));
                        break;
                    case "altitude":
                        position.setAltitude(Double.parseDouble(value));
                        break;
                    case "accuracy":
                        position.setAccuracy(Double.parseDouble(value));
                        break;
                    case "hdop":
                        position.set(Position.KEY_HDOP, Double.parseDouble(value));
                        break;
                    case "batt":
                        position.set(Position.KEY_BATTERY_LEVEL, Double.parseDouble(value));
                        break;
                    case "driverUniqueId":
                        position.set(Position.KEY_DRIVER_UNIQUE_ID, value);
                        break;
                    case "charge":
                        position.set(Position.KEY_CHARGE, Boolean.parseBoolean(value));
                        break;
                    default:
                        try {
                            position.set(entry.getKey(), Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            switch (value) {
                                case "true" -> position.set(entry.getKey(), true);
                                case "false" -> position.set(entry.getKey(), false);
                                default -> position.set(entry.getKey(), value);
                            }
                        }
                        break;
                }
            }
        }

        if (position.getFixTime() == null) {
            position.setTime(new Date());
        }

        if (network.getCellTowers() != null || network.getWifiAccessPoints() != null) {
            position.setNetwork(network);
        }

        if (latitude != null && longitude != null) {
            position.setLatitude(latitude);
            position.setLongitude(longitude);
        } else {
            getLastLocation(position, position.getDeviceTime());
        }

        sendResponse(channel, HttpResponseStatus.OK,
                Unpooled.copiedBuffer("OK", StandardCharsets.UTF_8));
        return position;
    }



}
