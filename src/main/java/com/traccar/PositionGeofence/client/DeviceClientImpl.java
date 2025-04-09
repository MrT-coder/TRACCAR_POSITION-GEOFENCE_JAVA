package com.traccar.PositionGeofence.client;

import com.traccar.PositionGeofence.modelo.Device;
import org.springframework.beans.factory.annotation.Value;
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
        String url = deviceServiceBaseUrl + "/devices/" + deviceId;
        return restTemplate.getForObject(url, Device.class);
    }
}