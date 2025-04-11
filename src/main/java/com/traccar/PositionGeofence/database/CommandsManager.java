// package com.traccar.PositionGeofence.database;

// import com.traccar.PositionGeofence.client.DeviceClient;
// import com.traccar.PositionGeofence.broadcast.BroadcastInterface;
// import com.traccar.PositionGeofence.broadcast.BroadcastService;
// import com.traccar.PositionGeofence.config.Config;
// import com.traccar.PositionGeofence.session.ConnectionManager;
// import com.traccar.PositionGeofence.session.DeviceSession;
// import com.traccar.PositionGeofence.storage.Storage;
// import com.traccar.PositionGeofence.storage.StorageException;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.Query;
// import org.springframework.stereotype.Component;

// import jakarta.annotation.Nullable;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.stream.Collectors;
// import java.util.List;
// import java.util.concurrent.TimeUnit;

// @Component
// public class CommandsManager implements BroadcastInterface {

//     private final Storage storage;
//     private final DeviceClient deviceClient; // En caso de obtener información vía REST
//     private final SmsManager smsManager;
//     private final ConnectionManager connectionManager;
//     private final BroadcastService broadcastService;
//     private final NotificationManager notificationManager;

//     @Autowired
//     public CommandsManager(Storage storage,
//                            @Nullable SmsManager smsManager,
//                            ConnectionManager connectionManager,
//                            BroadcastService broadcastService,
//                            NotificationManager notificationManager,
//                            DeviceClient deviceClient) {
//         this.storage = storage;
//         this.smsManager = smsManager;
//         this.connectionManager = connectionManager;
//         this.broadcastService = broadcastService;
//         this.notificationManager = notificationManager;
//         this.deviceClient = deviceClient;
//         broadcastService.registerListener(this);
//     }

//     /**
//      * Envía un comando al dispositivo.
//      * Si el comando es de texto se utiliza el SMSManager,
//      * de lo contrario se intenta enviar en vivo vía la sesión. Si la sesión no está activa y el comando
//      * permite ser encolado, se guarda en una cola (representada por QueuedCommand).
//      */
//     public QueuedCommand sendCommand(Command command) throws Exception {
//         long deviceId = command.getDeviceId();
//         if (command.getTextChannel()) {
//             if (smsManager == null) {
//                 throw new RuntimeException("SMS not configured");
//             }
//             // Obtener el dispositivo – se puede delegar a un DeviceClient o consultar el Storage
//             // Aquí se utiliza Storage con una consulta Mongo, asumiendo que en el microservicio se conserva la posición.
//             Query deviceQuery = new Query(Criteria.where("id").is(deviceId));
//             QueryRequest deviceRequest = new QueryRequest(deviceQuery);
//             Device device = storage.getObject(Device.class, deviceRequest);
//             // Obtener la última posición
//             Query positionQuery = new Query(Criteria.where("id").is(device.getPositionId()));
//             QueryRequest positionRequest = new QueryRequest(positionQuery);
//             Position position = storage.getObject(Position.class, positionRequest);
//             if (position != null) {
//                 // Suponiendo que de alguna forma se obtiene el protocolo para enviar un comando de texto.
//                 // Por ejemplo, podrías disponer de un método en BroadcastService o un bean Protocol que se encarga de ello.
//                 var protocol = broadcastService.getProtocolByName(position.getProtocol());
//                 protocol.sendTextCommand(device.getPhone(), command);
//             } else if (command.getType().equals(Command.TYPE_CUSTOM)) {
//                 smsManager.sendMessage(device.getPhone(), command.getString(Command.KEY_DATA), true);
//             } else {
//                 throw new RuntimeException("Command " + command.getType() + " is not supported");
//             }
//         } else {
//             // Se intenta obtener la sesión del dispositivo para enviar comandos en vivo.
//             DeviceSession deviceSession = connectionManager.getDeviceSession(deviceId);
//             if (deviceSession != null && deviceSession.supportsLiveCommands()) {
//                 deviceSession.sendCommand(command);
//             } else if (!command.getBoolean(Command.KEY_NO_QUEUE, false)) {
//                 // Si no hay sesión en vivo y se permite la cola, se encola el comando.
//                 QueuedCommand queuedCommand = QueuedCommand.fromCommand(command);
//                 // Se añade al Storage (MongoDB). En la consulta, se omite el campo "id" para que se asigne automáticamente.
//                 Query queueQuery = new Query(); // La consulta solo se usa aquí para construir un QueryRequest stub.
//                 QueryRequest queueRequest = new QueryRequest(queueQuery);
//                 queuedCommand.setId(storage.addObject(queuedCommand, queueRequest));
//                 broadcastService.updateCommand(true, deviceId);
//                 return queuedCommand;
//             } else {
//                 throw new RuntimeException("Failed to send command");
//             }
//         }
//         return null;
//     }

//     /**
//      * Lee los comandos encolados para un dispositivo.
//      */
//     public Collection<Command> readQueuedCommands(long deviceId) {
//         return readQueuedCommands(deviceId, Integer.MAX_VALUE);
//     }

//     public Collection<Command> readQueuedCommands(long deviceId, int count) {
//         Query query = new Query(Criteria.where("deviceId").is(deviceId));
//         query.with(Sort.by(Sort.Direction.DESC, "id"));
//         query.limit(count);
//         QueryRequest queryRequest = new QueryRequest(query);
//         var commands = storage.getObjects(QueuedCommand.class, queryRequest);
//         Map<Event, Position> events = new HashMap<>();
//         for (QueuedCommand queuedCommand : commands) {
//             Query removeQuery = new Query(Criteria.where("id").is(queuedCommand.getId()));
//             storage.removeObject(QueuedCommand.class, new QueryRequest(removeQuery));
//             Event event = new Event(Event.TYPE_QUEUED_COMMAND_SENT, queuedCommand.getDeviceId());
//             event.set("id", queuedCommand.getId());
//             events.put(event, null);
//         }
//         notificationManager.updateEvents(events);
//         return commands.stream().map(QueuedCommand::toCommand).collect(Collectors.toList());
//     }

//     @Override
//     public void updateCommand(boolean local, long deviceId) {
//         if (!local) {
//             DeviceSession deviceSession = connectionManager.getDeviceSession(deviceId);
//             if (deviceSession != null && deviceSession.supportsLiveCommands()) {
//                 for (Command command : readQueuedCommands(deviceId)) {
//                     deviceSession.sendCommand(command);
//                 }
//             }
//         }
//     }
// }