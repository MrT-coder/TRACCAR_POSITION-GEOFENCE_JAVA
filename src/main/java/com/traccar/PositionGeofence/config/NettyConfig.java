package com.traccar.PositionGeofence.config;


import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Configuration
public class NettyConfig {

    /**
     * Define un bean de tipo Timer utilizando la implementación HashedWheelTimer de Netty.
     *
     * @return una instancia de HashedWheelTimer.
     */
    @Bean
    public Timer nettyTimer() {
        // Puedes configurar los parámetros del HashedWheelTimer si lo requieres.
        return new HashedWheelTimer();
    }

    /**
     * Provee un Injector vacío (o con módulos, si los necesitas) 
     * para que ServerManager y cualquier otro @Autowired lo reciba.
     */
    @Bean
    public Injector injector() {
        return Guice.createInjector();
    }
}
