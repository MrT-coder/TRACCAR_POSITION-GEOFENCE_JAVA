package com.traccar.PositionGeofence.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF (opcional)
                .csrf(csrf -> csrf.disable())
                // Autorizamos cualquier request
                .authorizeHttpRequests(authz -> authz
                                .anyRequest().permitAll()
                )
                // Opcional: si no deseas login básico, puedes omitir httpBasic()
                .httpBasic(Customizer.withDefaults());

        // Construimos y devolvemos la configuración
        return http.build();
    }
}
