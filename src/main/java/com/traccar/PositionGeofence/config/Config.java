package com.traccar.PositionGeofence.config;


//import com.traccar.PositionGeofence.helper.Log;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.common.annotations.VisibleForTesting;

import java.util.Objects;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "traccar")
public class Config {

    private boolean useEnvironmentVariables;

    private final Properties properties = new Properties();

    @PostConstruct
    public void init() {
        // Si usas @ConfigurationProperties, las propiedades inyectadas (como useEnvironmentVariables) ya están disponibles.
        // Puedes volcar estas propiedades en el objeto Properties para compatibilidad con métodos existentes.
        properties.put("config.useEnvironmentVariables", Boolean.toString(useEnvironmentVariables));
        // Ejemplo: si agregas más propiedades, puedes volcar sus valores:
        // properties.put("some.property", someProperty);

    }

    public boolean hasKey(ConfigKey<?> key) {
        return hasKey(key.getKey());
    }

    private boolean hasKey(String key) {
        return useEnvironmentVariables && System.getenv().containsKey(getEnvironmentVariableName(key))
                || properties.containsKey(key);
    }

    public String getString(ConfigKey<String> key) {
        return getString(key.getKey(), key.getDefaultValue());
    }

    @Deprecated
    public String getString(String key) {
        if (useEnvironmentVariables) {
            String value = System.getenv(getEnvironmentVariableName(key));
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return properties.getProperty(key);
    }

    public String getString(ConfigKey<String> key, String defaultValue) {
        return getString(key.getKey(), defaultValue);
    }

    @Deprecated
    public String getString(String key, String defaultValue) {
        return hasKey(key) ? getString(key) : defaultValue;
    }

    public boolean getBoolean(ConfigKey<Boolean> key) {
        String value = getString(key.getKey());
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            Boolean defaultValue = key.getDefaultValue();
            return Objects.requireNonNullElse(defaultValue, false);
        }
    }

    public int getInteger(ConfigKey<Integer> key) {
        String value = getString(key.getKey());
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            Integer defaultValue = key.getDefaultValue();
            return Objects.requireNonNullElse(defaultValue, 0);
        }
    }

    public int getInteger(ConfigKey<Integer> key, int defaultValue) {
        return getInteger(key.getKey(), defaultValue);
    }

    @Deprecated
    public int getInteger(String key, int defaultValue) {
        return hasKey(key) ? Integer.parseInt(getString(key)) : defaultValue;
    }

    public long getLong(ConfigKey<Long> key) {
        String value = getString(key.getKey());
        if (value != null) {
            return Long.parseLong(value);
        } else {
            Long defaultValue = key.getDefaultValue();
            return Objects.requireNonNullElse(defaultValue, 0L);
        }
    }

    public double getDouble(ConfigKey<Double> key) {
        String value = getString(key.getKey());
        if (value != null) {
            return Double.parseDouble(value);
        } else {
            Double defaultValue = key.getDefaultValue();
            return Objects.requireNonNullElse(defaultValue, 0.0);
        }
    }

    @VisibleForTesting
    public void setString(ConfigKey<?> key, String value) {
        properties.put(key.getKey(), value);
    }

    static String getEnvironmentVariableName(String key) {
        return key.replaceAll("\\.", "_").replaceAll("(\\p{Lu})", "_$1").toUpperCase();
    }

     // getters / setters para @ConfigurationProperties

     public boolean isUseEnvironmentVariables() {
        return useEnvironmentVariables;
    }

    public void setUseEnvironmentVariables(boolean useEnvironmentVariables) {
        this.useEnvironmentVariables = useEnvironmentVariables;
    }
}