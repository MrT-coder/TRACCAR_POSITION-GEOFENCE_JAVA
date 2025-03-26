package com.traccar.PositionGeofence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.traccar.PositionGeofence.repositorio")
@SpringBootApplication
public class PositionGeofenceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PositionGeofenceApplication.class, args);
	}

}
