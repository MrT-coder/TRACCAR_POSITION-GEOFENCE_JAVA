package com.traccar.PositionGeofence.modelo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QueuedCommand extends BaseCommand {

    public static QueuedCommand fromCommand(Command command) {
        QueuedCommand queuedCommand = new QueuedCommand();
        queuedCommand.setDeviceId(command.getDeviceId());
        queuedCommand.setType(command.getType());
        queuedCommand.setTextChannel(command.getTextChannel());
        queuedCommand.setAttributes(new HashMap<>(command.getAttributes()));
        return queuedCommand;
    }

    public Command toCommand() {
        Command command = new Command();
        command.setDeviceId(getDeviceId());
        command.setType(getType());
        command.setDescription("");
        command.setTextChannel(getTextChannel());
        command.setAttributes(new HashMap<>(getAttributes()));
        return command;
    }

}
