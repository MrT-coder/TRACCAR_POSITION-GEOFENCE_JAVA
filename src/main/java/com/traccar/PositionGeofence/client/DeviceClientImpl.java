package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.Device;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DeviceClientImpl implements DeviceClient {

    private final RestTemplate restTemplate;
    
    
    private final String deviceServiceBaseUrl;

    public DeviceClientImpl(@Value("${gateway.device.baseUrl}") String deviceServiceBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.deviceServiceBaseUrl = deviceServiceBaseUrl;
    }

    // PUT para actualizar un dispositivO
    @Override
    public Device updateDevice(Device device) {
        String url = deviceServiceBaseUrl + "/devices/" + device.getId();
        // Creamos una entidad Http con el dispositivo a actualizar.
        HttpEntity<Device> entity = new HttpEntity<>(device);
        // Usamos exchange para enviar una petición PUT y obtener la respuesta en un
        // ResponseEntity.
        ResponseEntity<Device> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Device.class);
        return response.getBody();
    }

    // GET para obtener un dispositivo por el ID del usuario
    @Override
    public List<Device> getDevicesByUser(long userId) throws Exception {
        String url = deviceServiceBaseUrl + "/devices/" + userId;
        ResponseEntity<List<Device>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Device>>() {
                });
        return response.getBody();
    }

    // GET para obtener todos los dispositivos
    @Override
    public List<Device> getDevices() {
        String url = deviceServiceBaseUrl + "/devices/";
        ResponseEntity<List<Device>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Device>>() {
                });
        return response.getBody();
    }

    // GET para obtener un dispositivo por su ID
    @Override
    public Device getDeviceById(long deviceId) throws Exception {
        String url = deviceServiceBaseUrl + "/devices/" + deviceId;
        ResponseEntity<Device> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Device.class);
        return response.getBody();
    }

    // GET para obtener un dispositivo por su uniqueId
    @Override
    public Device getDevicesByUniqueId(String uniqueId) throws Exception {
        String url = deviceServiceBaseUrl + "/devices/" + uniqueId;
        ResponseEntity<Device> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Device>() {
                });
        return response.getBody();
    }
}