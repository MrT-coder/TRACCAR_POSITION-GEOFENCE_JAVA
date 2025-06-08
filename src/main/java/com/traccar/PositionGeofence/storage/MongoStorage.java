package com.traccar.PositionGeofence.storage;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Permission;

import java.util.List;

@Component
public class MongoStorage implements Storage {

    private final MongoTemplate mongoTemplate;

    public MongoStorage(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public <T> List<T> getObjects(Class<T> clazz, QueryRequest request) throws StorageException {
        try {
            if (request == null || request.getQuery() == null) {
                return mongoTemplate.findAll(clazz);
            } else {
                return mongoTemplate.find(request.getQuery(), clazz);
            }
        } catch (Exception e) {
            throw new StorageException("Error executing getObjects", e);
        }
    }

    @Override
    public <T> String addObject(T entity) throws StorageException {
        try {
            T inserted = mongoTemplate.insert(entity);
            // Se asume que tu modelo tiene un campo @Id de tipo String (o ajusta según
            // corresponda)
            // Aquí puedes obtener el id a través de reflection o suponiendo un método
            // getId().
            return inserted.toString();
        } catch (Exception e) {
            throw new StorageException("Error executing addObject", e);
        }
    }

    @Override
    public <T> void updateObject(T entity) throws StorageException {
        try {
            mongoTemplate.save(entity);
        } catch (Exception e) {
            throw new StorageException("Error executing updateObject", e);
        }
    }

    @Override
    public void removeObject(Class<?> clazz, QueryRequest request) throws StorageException {
        try {
            if (request == null || request.getQuery() == null) {
                throw new StorageException("Query is required for removeObject");
            }
            mongoTemplate.remove(request.getQuery(), clazz);
        } catch (Exception e) {
            throw new StorageException("Error executing removeObject", e);
        }
    }

    public <T> T getObject(Class<T> clazz, QueryRequest request) throws StorageException {
        var objects = getObjects(clazz, request);
        return objects.isEmpty() ? null : objects.get(0);
    }

    @Override
    public List<Permission> getPermissions(Class<? extends BaseModel> ownerClass, long ownerId,
            Class<? extends BaseModel> propertyClass, long propertyId) throws StorageException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPermissions'");
    }

    @Override
    public void addPermission(Permission permission) throws StorageException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addPermission'");
    }

    @Override
    public void removePermission(Permission permission) throws StorageException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removePermission'");
    }
    

}