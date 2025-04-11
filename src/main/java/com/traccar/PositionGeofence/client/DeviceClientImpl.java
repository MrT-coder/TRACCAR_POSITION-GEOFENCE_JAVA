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

    @Override
    public Device getDeviceById(String deviceId) {
        String url = deviceServiceBaseUrl + "/device/" + deviceId;
        return restTemplate.getForObject(url, Device.class);
    }

    @Override
    public Device updateDevice(Device device) {
        String url = deviceServiceBaseUrl + "/device/" + device.getUniqueId();
        // Creamos una entidad Http con el dispositivo a actualizar.
        HttpEntity<Device> entity = new HttpEntity<>(device);
        // Usamos exchange para enviar una petición PUT y obtener la respuesta en un
        // ResponseEntity.
        ResponseEntity<Device> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Device.class);
        return response.getBody();
    }

    @Override
    public List<Device> getDevicesForUser(long userId) throws Exception {
        String url = deviceServiceBaseUrl + "/device/user/" + userId;
        ResponseEntity<List<Device>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Device>>() {
                });
        return response.getBody();
    }
}