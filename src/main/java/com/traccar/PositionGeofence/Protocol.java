package com.traccar.PositionGeofence;

import io.netty.channel.Channel;
import java.net.SocketAddress;
import java.util.Collection;

import com.traccar.PositionGeofence.modelo.Command;

public interface Protocol {

    String getName();

    Collection<TrackerConnector> getConnectorList();

    Collection<String> getSupportedDataCommands();

    void sendDataCommand(Channel channel, SocketAddress remoteAddress, Command command);

    Collection<String> getSupportedTextCommands();

    void sendTextCommand(String destAddress, Command command) throws Exception;

}
