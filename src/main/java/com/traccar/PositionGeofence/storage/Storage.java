package com.traccar.PositionGeofence.storage;


import java.util.List;

public abstract class Storage {

    public abstract <T> List<T> getObjects(Class<T> clazz, Request request) throws StorageException;

    public abstract <T> long addObject(T entity, Request request) throws StorageException;

    public abstract <T> void updateObject(T entity, Request request) throws StorageException;

    public abstract void removeObject(Class<?> clazz, Request request) throws StorageException;

    public abstract List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException;

    public abstract void addPermission(Permission permission) throws StorageException;

    public abstract void removePermission(Permission permission) throws StorageException;

    public List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass,
            Class<? extends BaseModel> propertyClass) throws StorageException {
        return getPermissions(ownerClass, 0, propertyClass, 0);
    }

    public <T> T getObject(Class<T> clazz, Request request) throws StorageException {
        var objects = getObjects(clazz, request);
        return objects.isEmpty() ? null : objects.get(0);
    }

}
