package com.traccar.PositionGeofence.config;


import com.traccar.PositionGeofence.helper.Log;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "traccar")
public class Config {

    private boolean useEnvironmentVariables;

    // Si necesitas otras propiedades de configuración, defínelas aquí.
    // Por ejemplo:
    private String someProperty;

    // Además, usamos una instancia de Properties para poder reutilizar la lógica de getString, getInteger, etc.
    private final Properties properties = new Properties();

    @PostConstruct
    public void init() {
        // Si usas @ConfigurationProperties, las propiedades inyectadas (como useEnvironmentVariables) ya están disponibles.
        // Puedes volcar estas propiedades en el objeto Properties para compatibilidad con métodos existentes.
        properties.put("config.useEnvironmentVariables", Boolean.toString(useEnvironmentVariables));
        // Ejemplo: si agregas más propiedades, puedes volcar sus valores:
        // properties.put("some.property", someProperty);

        // Configura el logger según la configuración (esto es opcional, según cómo manejes Log)
        Log.setupLogger(this);
    }

    public boolean hasKey(String key) {
        return (useEnvironmentVariables && System.getenv().containsKey(getEnvironmentVariableName(key)))
                || properties.containsKey(key);
    }

    public String getString(String key, String defaultValue) {
        if (useEnvironmentVariables) {
            String value = System.getenv(getEnvironmentVariableName(key));
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return properties.getProperty(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public int getInteger(String key, int defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    public double getDouble(String key, double defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            return Double.parseDouble(value);
        }
        return defaultValue;
    }

    // Método de prueba para ajustar valores (útil en tests)
    public void setString(String key, String value) {
        properties.put(key, value);
    }

    static String getEnvironmentVariableName(String key) {
        return key.replaceAll("\\.", "_").replaceAll("(\\p{Lu})", "_$1").toUpperCase();
    }

    // Getters y setters para las propiedades inyectadas mediante @ConfigurationProperties
    public boolean isUseEnvironmentVariables() {
        return useEnvironmentVariables;
    }

    public void setUseEnvironmentVariables(boolean useEnvironmentVariables) {
        this.useEnvironmentVariables = useEnvironmentVariables;
    }

    public String getSomeProperty() {
        return someProperty;
    }

    public void setSomeProperty(String someProperty) {
        this.someProperty = someProperty;
        properties.put("some.property", someProperty);  // Opcional: volcarlo en el objeto properties
    }
}