package com.traccar.PositionGeofence;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.protocol.BaseProtocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServerManager implements LifecycleObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);

    private final List<TrackerConnector> connectorList = new LinkedList<>();
    private final Map<String, BaseProtocol> protocolList = new ConcurrentHashMap<>();

    @Autowired
    public ServerManager(
            Config config, List<BaseProtocol> protocols)
            throws IOException, URISyntaxException, ReflectiveOperationException {
        Set<String> enabledProtocols = null;
        if (config.hasKey(Keys.PROTOCOLS_ENABLE)) {
            enabledProtocols = new HashSet<>(Arrays.asList(config.getString(Keys.PROTOCOLS_ENABLE).split("[, ]")));
        }
        for (BaseProtocol protocol : protocols) {
            String protocolName = protocol.getName();
            if (enabledProtocols == null || enabledProtocols.contains(protocolName)) {
                int port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix(protocolName));
                if (port > 0) {
                    connectorList.addAll(protocol.getConnectorList());
                    protocolList.put(protocolName, protocol);
                    LOGGER.debug("Configurar protocolo {} en puerto {}", protocolName, port);
                }
            }
        }
    }

    public BaseProtocol getProtocol(String name) {
        return protocolList.get(name);
    }

    @Override
    public void start() throws Exception {
        for (TrackerConnector connector : connectorList) {
            try {
                connector.start();
            } catch (BindException e) {
                LOGGER.warn("Error en el puerto", e);
            } catch (ConnectException e) {
                LOGGER.warn("No pudo arrancar conector", e);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        for (TrackerConnector connector : connectorList) {
            try {
                connector.stop();
            } catch (Exception e) {
                LOGGER.warn("No pudo parar conector", e);
            }
        }
    }

}
