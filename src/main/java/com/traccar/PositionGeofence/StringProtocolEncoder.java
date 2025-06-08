package com.traccar.PositionGeofence;

import com.traccar.PositionGeofence.modelo.Command;
import com.traccar.PositionGeofence.protocol.BaseProtocolEncoder;

public abstract class StringProtocolEncoder extends BaseProtocolEncoder {

    public StringProtocolEncoder(Protocol protocol) {
        super(protocol);
    }

    public interface ValueFormatter {
        String formatValue(String key, Object value);
    }

    protected String formatCommand(Command command, String format, ValueFormatter valueFormatter, String... keys) {

        Object[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String value = null;
            if (keys[i].equals(Command.KEY_UNIQUE_ID)) {
                value = getUniqueId(command.getDeviceId());
            } else {
                Object object = command.getAttributes().get(keys[i]);
                if (valueFormatter != null) {
                    value = valueFormatter.formatValue(keys[i], object);
                }
                if (value == null && object != null) {
                    value = object.toString();
                }
                if (value == null) {
                    value = "";
                }
            }
            values[i] = value;
        }

        return String.format(format, values);
    }

    protected String formatCommand(Command command, String format, String... keys) {
        return formatCommand(command, format, null, keys);
    }

}