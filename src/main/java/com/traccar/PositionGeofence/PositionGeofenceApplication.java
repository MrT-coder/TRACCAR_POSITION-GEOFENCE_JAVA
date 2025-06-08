package com.traccar.PositionGeofence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.google.inject.Injector;
import com.traccar.PositionGeofence.web.WebServer;

@EnableMongoRepositories(basePackages = "com.traccar.PositionGeofence.repositorio")
@SpringBootApplication
public class PositionGeofenceApplication{
	 private static final Logger LOGGER = LoggerFactory.getLogger(PositionGeofenceApplication.class);
	 private static Injector injector;
	 public static Injector getInjector() {
        return injector;
    }

	public static void main(String[] args) {
		SpringApplication.run(PositionGeofenceApplication.class, args);
		LOGGER.info("PositionGeofence microservice started successfully");
	}

	@Bean
    public CommandLineRunner starter(ServerManager serverManager, WebServer webServer) {
        return args -> {
            // Arrancamos primero el ServerManager (abre puertos Netty, etc)
            try {
                serverManager.start();
                LOGGER.info("ServerManager iniciado");
            } catch (Exception e) {
                LOGGER.error("Error arrancando ServerManager", e);
                // quizá quieras System.exit(1) si es crítico
            }
            // Luego levantamos el WebServer (Jetty, API REST, proxy, etc)
            try {
                webServer.start();
                LOGGER.info("WebServer iniciado" );
            } catch (Exception e) {
                LOGGER.error("Error arrancando WebServer", e);
            }

            // Hook para parada limpia al cerrar la JVM
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Deteniendo servicios...");
                try {
                    serverManager.stop();
                    webServer.stop();
                } catch (Exception ex) {
                    LOGGER.error("Error parando servicios", ex);
                }
            }));
        };
    }

}
