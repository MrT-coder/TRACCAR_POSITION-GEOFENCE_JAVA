package com.traccar.PositionGeofence.database;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.traccar.PositionGeofence.broadcast.BroadcastInterface;
import com.traccar.PositionGeofence.session.ConnectionManager;

@Singleton
public class CommandsManager implements BroadcastInterface {

    private final ServerManager serverManager;
    private final SmsManager smsManager;
    private final ConnectionManager connectionManager;
    private final BroadcastService broadcastService;
    private final NotificationManager notificationManager;

    @Inject
    public CommandsManager(
            Storage storage, ServerManager serverManager, @Nullable SmsManager smsManager,
            ConnectionManager connectionManager, BroadcastService broadcastService,
            NotificationManager notificationManager) {
        this.storage = storage;
        this.serverManager = serverManager;
        this.smsManager = smsManager;
        this.connectionManager = connectionManager;
        this.broadcastService = broadcastService;
        this.notificationManager = notificationManager;
        broadcastService.registerListener(this);
    }

    public QueuedCommand sendCommand(Command command) throws Exception {
        long deviceId = command.getDeviceId();
        if (command.getTextChannel()) {
            if (smsManager == null) {
                throw new RuntimeException("SMS not configured");
            }
            Device device = storage.getObject(Device.class, new Request(
                    new Columns.Include("positionId", "phone"), new Condition.Equals("id", deviceId)));
            Position position = storage.getObject(Position.class, new Request(
                    new Columns.All(), new Condition.Equals("id", device.getPositionId())));
            if (position != null) {
                BaseProtocol protocol = serverManager.getProtocol(position.getProtocol());
                protocol.sendTextCommand(device.getPhone(), command);
            } else if (command.getType().equals(Command.TYPE_CUSTOM)) {
                smsManager.sendMessage(device.getPhone(), command.getString(Command.KEY_DATA), true);
            } else {
                throw new RuntimeException("Command " + command.getType() + " is not supported");
            }
        } else {
            DeviceSession deviceSession = connectionManager.getDeviceSession(deviceId);
            if (deviceSession != null && deviceSession.supportsLiveCommands()) {
                deviceSession.sendCommand(command);
            } else if (!command.getBoolean(Command.KEY_NO_QUEUE)) {
                QueuedCommand queuedCommand = QueuedCommand.fromCommand(command);
                queuedCommand.setId(storage.addObject(queuedCommand, new Request(new Columns.Exclude("id"))));
                broadcastService.updateCommand(true, deviceId);
                return queuedCommand;
            } else {
                throw new RuntimeException("Failed to send command");
            }
        }
        return null;
    }

    public Collection<Command> readQueuedCommands(long deviceId) {
        return readQueuedCommands(deviceId, Integer.MAX_VALUE);
    }

    public Collection<Command> readQueuedCommands(long deviceId, int count) {
        try {
            var commands = storage.getObjects(QueuedCommand.class, new Request(
                    new Columns.All(),
                    new Condition.Equals("deviceId", deviceId),
                    new Order("id", false, count)));
            Map<Event, Position> events = new HashMap<>();
            for (var command : commands) {
                storage.removeObject(QueuedCommand.class, new Request(
                        new Condition.Equals("id", command.getId())));

                Event event = new Event(Event.TYPE_QUEUED_COMMAND_SENT, command.getDeviceId());
                event.set("id", command.getId());
                events.put(event, null);
            }
            notificationManager.updateEvents(events);
            return commands.stream().map(QueuedCommand::toCommand).collect(Collectors.toList());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateCommand(boolean local, long deviceId) {
        if (!local) {
            DeviceSession deviceSession = connectionManager.getDeviceSession(deviceId);
            if (deviceSession != null && deviceSession.supportsLiveCommands()) {
                for (Command command : readQueuedCommands(deviceId)) {
                    deviceSession.sendCommand(command);
                }
            }
        }
    }

}
