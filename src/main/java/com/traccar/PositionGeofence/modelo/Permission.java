package com.traccar.PositionGeofence.modelo;

import java.beans.Introspector;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.traccar.PositionGeofence.helper.ClassScanner;


public class Permission {

    private static final Map<String, Class<? extends BaseModel>> CLASSES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        try {
            for (Class<?> clazz : ClassScanner.findSubclasses(BaseModel.class)) {
                CLASSES.put(clazz.getSimpleName(), (Class<? extends BaseModel>) clazz);
            }
        } catch (IOException | ReflectiveOperationException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final LinkedHashMap<String, Long> data;

    private final Class<? extends BaseModel> ownerClass;
    private final long ownerId;
    private final Class<? extends BaseModel> propertyClass;
    private final long propertyId;

    public Permission(LinkedHashMap<String, Long> data) {
        this.data = data;
        var iterator = data.entrySet().iterator();
        var owner = iterator.next();
        ownerClass = getKeyClass(owner.getKey());
        ownerId = owner.getValue();
        var property = iterator.next();
        propertyClass = getKeyClass(property.getKey());
        propertyId = property.getValue();
    }

    public Permission(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) {
        this.ownerClass = ownerClass;
        this.ownerId = ownerId;
        this.propertyClass = propertyClass;
        this.propertyId = propertyId;
        data = new LinkedHashMap<>();
        data.put(getKey(ownerClass), ownerId);
        data.put(getKey(propertyClass), propertyId);
    }

    public static Class<? extends BaseModel> getKeyClass(String key) {
        return CLASSES.get(key.substring(0, key.length() - 2));
    }

    public static String getKey(Class<?> clazz) {
        return Introspector.decapitalize(clazz.getSimpleName()) + "Id";
    }

    public static String getStorageName(Class<?> ownerClass, Class<?> propertyClass) {
        String ownerName = ownerClass.getSimpleName();
        String propertyName = propertyClass.getSimpleName();
        String managedPrefix = "Managed";
        if (propertyName.startsWith(managedPrefix)) {
            propertyName = propertyName.substring(managedPrefix.length());
        }
        return "tc_" + Introspector.decapitalize(ownerName) + "_" + Introspector.decapitalize(propertyName);
    }


    @JsonIgnore
    public String getStorageName() {
        return getStorageName(ownerClass, propertyClass);
    }


    @JsonAnyGetter
    public Map<String, Long> get() {
        return data;
    }


    @JsonAnySetter
    public void set(String key, Long value) {
        data.put(key, value);
    }


    @JsonIgnore
    public Class<? extends BaseModel> getOwnerClass() {
        return ownerClass;
    }


    @JsonIgnore
    public long getOwnerId() {
        return ownerId;
    }

 
    @JsonIgnore
    public Class<? extends BaseModel> getPropertyClass() {
        return propertyClass;
    }


    @JsonIgnore
    public long getPropertyId() {
        return propertyId;
    }

}
