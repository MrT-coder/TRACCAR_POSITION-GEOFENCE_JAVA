package com.traccar.PositionGeofence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = "com.traccar.PositionGeofence.repositorio")
@SpringBootApplication
public class PositionGeofenceApplication {
	 private static final Logger LOGGER = LoggerFactory.getLogger(PositionGeofenceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PositionGeofenceApplication.class, args);
		LOGGER.info("PositionGeofence microservice started successfully");
	}

}
