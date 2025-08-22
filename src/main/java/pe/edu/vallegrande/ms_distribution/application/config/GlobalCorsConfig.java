package pe.edu.vallegrande.ms_distribution.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir cualquier origen
        config.addAllowedOrigin("*");
        // O también: config.setAllowedOriginPatterns(List.of("*"));

        // Permitir todos los métodos
        config.addAllowedMethod("*");

        // Permitir todos los headers
        config.addAllowedHeader("*");

        // No usar credenciales para que funcione con "*"
        config.setAllowCredentials(false);

        config.setMaxAge(3600L); // Cache del preflight por 1h

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
