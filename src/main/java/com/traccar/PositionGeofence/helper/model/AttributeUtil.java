package com.traccar.PositionGeofence.helper.model;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.ConfigKey;
import com.traccar.PositionGeofence.config.KeyType;
import com.traccar.PositionGeofence.config.Keys;
import com.traccar.PositionGeofence.modelo.Device;
import com.traccar.PositionGeofence.modelo.Group;
import com.traccar.PositionGeofence.modelo.Server;
import com.traccar.PositionGeofence.session.cache.CacheManager;

public final class AttributeUtil {

    private AttributeUtil() {
    }

    /**
     * Interfaz para definir el contexto de búsqueda (provee los objetos necesarios).
     */
    public interface Provider {
        Device getDevice();
        Group getGroup(long groupId);
        Server getServer();
        Config getConfig();
    }

    /**
     * Método principal para buscar un atributo configurado.
     * Primero se busca en el Device, luego recorre la jerarquía de grupos (si aplica), 
     * luego en el Server y finalmente en la Config.
     */
    public static <T> T lookup(CacheManager cacheManager, ConfigKey<T> key, long deviceId) {
        return lookup(new CacheProvider(cacheManager, deviceId), key);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T lookup(Provider provider, ConfigKey<T> key) {
        Device device = provider.getDevice();
        Object result = device.getAttributes().get(key.getKey());
        long groupId = device.getGroupId();
        while (result == null && groupId > 0) {
            Group group = provider.getGroup(groupId);
            if (group != null) {
                result = group.getAttributes().get(key.getKey());
                groupId = group.getGroupId();
            } else {
                groupId = 0;
            }
        }
        if (result == null && key.hasType(KeyType.SERVER)) {
            result = provider.getServer().getAttributes().get(key.getKey());
        }
        if (result == null && key.hasType(KeyType.CONFIG)) {
            result = provider.getConfig().getString(key.getKey(), key.getDefaultValue() != null ? key.getDefaultValue().toString() : null);
        }

        if (result != null) {
            Class<T> valueClass = key.getValueClass();
            if (valueClass.equals(Boolean.class)) {
                return (T) (result instanceof String ? Boolean.parseBoolean((String) result) : result);
            } else if (valueClass.equals(Integer.class)) {
                return valueClass.cast(result instanceof String ? Integer.parseInt((String) result) : ((Number) result).intValue());
            } else if (valueClass.equals(Long.class)) {
                return key.getValueClass().cast(result instanceof String ? Long.parseLong((String) result) : ((Number) result).longValue());
            } else if (valueClass.equals(Double.class)) {
                return (T) (result instanceof String ? Double.valueOf(Double.parseDouble((String) result)) : Double.valueOf(((Number) result).doubleValue()));
            } else {
                return (T) result;
            }
        }
        return key.getDefaultValue();
    }

    /**
     * Método de utilidad para obtener la contraseña del dispositivo.
     */
    public static String getDevicePassword(CacheManager cacheManager, long deviceId, String protocol, String defaultPassword) {
        String password = lookup(cacheManager, Keys.DEVICE_PASSWORD, deviceId);
        if (password != null) {
            return password;
        }
        if (protocol != null) {
            password = cacheManager.getConfig().getString(Keys.PROTOCOL_DEVICE_PASSWORD.withPrefix(protocol));
            if (password != null) {
                return password;
            }
        }
        return defaultPassword;
    }

    /**
     * Implementación básica de Provider basada en el CacheManager.
     */
    public static class CacheProvider implements Provider {

        private final CacheManager cacheManager;
        private final long deviceId;

        public CacheProvider(CacheManager cacheManager, long deviceId) {
            this.cacheManager = cacheManager;
            this.deviceId = deviceId;
        }

        @Override
        public Device getDevice() {
            return cacheManager.getObject(Device.class, deviceId);
        }

        @Override
        public Group getGroup(long groupId) {
            return cacheManager.getObject(Group.class, groupId);
        }

        @Override
        public Server getServer() {
            try {
                return cacheManager.getServer();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get server from cacheManager", e);
            }
        }

        @Override
        public Config getConfig() {
            return cacheManager.getConfig();
        }
    }

    /**
     * Si en tu microservicio llegase a usarse el Storage para obtener atributos (por ejemplo, en caso de cache vacía),
     * podrías definir una implementación Provider que consulte el Storage.
     */
    // public static class StorageProvider implements Provider {

    //     private final Config config;
    //     private final Storage storage;
    //     // Si manejas permisos vía REST, aquí podrías inyectar un cliente de permisos
    //     // private final PermissionsService permissionsService;
    //     private final Device device;

    //     public StorageProvider(Config config, Storage storage, Device device) {
    //         this.config = config;
    //         this.storage = storage;
    //         this.device = device;
    //     }

    //     @Override
    //     public Device getDevice() {
    //         return device;
    //     }

    //     @Override
    //     public Group getGroup(long groupId) {
    //         return groupClient.getGroupById(groupId);
    //     }

    //     @Override
    //     public Server getServer() {
    //         return ServerClient.getServersById(1L);
    //     }

    //     @Override
    //     public Config getConfig() {
    //         return config;
    //     }
    // }
}