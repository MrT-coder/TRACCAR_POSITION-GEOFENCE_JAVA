package com.traccar.PositionGeofence.storage;

package com.traccar.PositionGeofence.storage;

import com.traccar.PositionGeofence.modelo.BaseModel;
import com.traccar.PositionGeofence.modelo.Permission;
import com.traccar.PositionGeofence.storage.query.Request;
import com.traccar.PositionGeofence.storage.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class RestStorageAdapter extends Storage {

    private final RestTemplate restTemplate;

    @Value("${storage.baseUrl}")
    private String storageBaseUrl;

    public RestStorageAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> List<T> getObjects(Class<T> clazz, Request request) throws StorageException {
        try {
            // Se asume que el servicio de almacenamiento espera el nombre de la clase y una representación
            // de la consulta (request) como parámetros de consulta
            String url = String.format("%s/objects?class=%s&%s",
                    storageBaseUrl, clazz.getSimpleName(), request.toQueryString());
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            return response.getBody();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public <T> long addObject(T entity, Request request) throws StorageException {
        try {
            String url = String.format("%s/objects?%s", storageBaseUrl, request.toQueryString());
            ResponseEntity<Long> response = restTemplate.postForEntity(url, entity, Long.class);
            return response.getBody();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public <T> void updateObject(T entity, Request request) throws StorageException {
        try {
            String url = String.format("%s/objects?%s", storageBaseUrl, request.toQueryString());
            HttpEntity<T> httpEntity = new HttpEntity<>(entity);
            restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removeObject(Class<?> clazz, Request request) throws StorageException {
        try {
            String url = String.format("%s/objects?class=%s&%s",
                    storageBaseUrl, clazz.getSimpleName(), request.toQueryString());
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<Permission> getPermissions(Class<? extends BaseModel> ownerClass, long ownerId,
                                             Class<? extends BaseModel> propertyClass, long propertyId)
            throws StorageException {
        try {
            String url = String.format("%s/permissions?ownerClass=%s&ownerId=%d&propertyClass=%s&propertyId=%d",
                    storageBaseUrl,
                    ownerClass.getSimpleName(), ownerId,
                    propertyClass.getSimpleName(), propertyId);
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            return response.getBody();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void addPermission(Permission permission) throws StorageException {
        try {
            String url = storageBaseUrl + "/permissions";
            restTemplate.postForEntity(url, permission, Void.class);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public void removePermission(Permission permission) throws StorageException {
        try {
            // Se asume que la propiedad 'id' de Permission está definida
            String url = storageBaseUrl + "/permissions/" + permission.getId();
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
}
