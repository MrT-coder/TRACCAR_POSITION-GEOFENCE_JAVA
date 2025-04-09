package com.traccar.PositionGeofence.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ConfigKey<T> {

    private final String key;
    private final Set<KeyType> types = new HashSet<>();
    private final Class<T> valueClass;
    private final T defaultValue;

    ConfigKey(String key, List<KeyType> types, Class<T> valueClass, T defaultValue) {
        this.key = key;
        this.types.addAll(types);
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public boolean hasType(KeyType type) {
        return types.contains(type);
    }

    public Class<T> getValueClass() {
        return valueClass;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

}

class StringConfigKey extends ConfigKey<String> {
    StringConfigKey(String key, List<KeyType> types) {
        super(key, types, String.class, null);
    }
    StringConfigKey(String key, List<KeyType> types, String defaultValue) {
        super(key, types, String.class, defaultValue);
    }
}

class BooleanConfigKey extends ConfigKey<Boolean> {
    BooleanConfigKey(String key, List<KeyType> types) {
        super(key, types, Boolean.class, null);
    }
    BooleanConfigKey(String key, List<KeyType> types, Boolean defaultValue) {
        super(key, types, Boolean.class, defaultValue);
    }
}

class IntegerConfigKey extends ConfigKey<Integer> {
    IntegerConfigKey(String key, List<KeyType> types) {
        super(key, types, Integer.class, null);
    }
    IntegerConfigKey(String key, List<KeyType> types, Integer defaultValue) {
        super(key, types, Integer.class, defaultValue);
    }
}

class LongConfigKey extends ConfigKey<Long> {
    LongConfigKey(String key, List<KeyType> types) {
        super(key, types, Long.class, null);
    }
    LongConfigKey(String key, List<KeyType> types, Long defaultValue) {
        super(key, types, Long.class, defaultValue);
    }
}

class DoubleConfigKey extends ConfigKey<Double> {
    DoubleConfigKey(String key, List<KeyType> types) {
        super(key, types, Double.class, null);
    }
    DoubleConfigKey(String key, List<KeyType> types, Double defaultValue) {
        super(key, types, Double.class, defaultValue);
    }
}