package com.traccar.PositionGeofence.storage;

import java.util.List;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Permission;

public interface Storage {

    /**
     * Recupera una lista de objetos del tipo T según los criterios de consulta.
     * Si request es null, se retornan todos los documentos de la colección.
     */
    <T> List<T> getObjects(Class<T> clazz, QueryRequest request) throws StorageException;

    /**
     * Inserta un objeto en la base de datos.
     * Devuelve el identificador generado.
     */
    <T> String addObject(T entity) throws StorageException;

    /**
     * Actualiza o guarda el objeto.
     */
    <T> void updateObject(T entity) throws StorageException;

    /**
     * Remueve los objetos del tipo clazz que cumplan la condición expresada en request.
     */
    void removeObject(Class<?> clazz, QueryRequest request) throws StorageException;

    <T> T getObject(Class<T> clazz, QueryRequest request) throws StorageException;

     public abstract List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException;

    public abstract void addPermission(Permission permission) throws StorageException;

    public abstract void removePermission(Permission permission) throws StorageException;

    public default List<Permission> getPermissions(
            Class<? extends BaseModel> ownerClass,
            Class<? extends BaseModel> propertyClass) throws StorageException {
        return getPermissions(ownerClass, 0, propertyClass, 0);
    }
    // Opcional: Métodos para permisos, según si decides implementarlos.
    // Por ejemplo:
    // List<Permission> getPermissions(...);
    // void addPermission(Permission permission) throws StorageException;
    // void removePermission(Permission permission) throws StorageException;
}