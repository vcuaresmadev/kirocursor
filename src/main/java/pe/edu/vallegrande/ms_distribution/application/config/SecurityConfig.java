package pe.edu.vallegrande.ms_distribution.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .authorizeExchange(authz -> authz
                // Endpoints públicos (solo para desarrollo)
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/actuator/info").permitAll()
                
                // Endpoints de autenticación
                .pathMatchers("/api/auth/**").permitAll()
                
                // Endpoints de la API que requieren autenticación
                .pathMatchers("/api/**").authenticated()
                
                // Endpoints de Swagger (solo en desarrollo)
                .pathMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Denegar acceso a endpoints sensibles
                .pathMatchers("/actuator/**").denyAll()
                .pathMatchers("/error").denyAll()
                
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable()) // Deshabilitar autenticación básica
            .formLogin(formLogin -> formLogin.disable()) // Deshabilitar formulario de login
            .logout(logout -> logout.disable()); // Deshabilitar logout

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Factor de costo 12 para mayor seguridad
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Configuración CORS más restrictiva
        List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",     // Frontend local
            "http://localhost:8080",     // Otros servicios locales
            "https://vallegrande.edu.pe", // Dominio de producción
            "https://*.vallegrande.edu.pe" // Subdominios
        );
        config.setAllowedOriginPatterns(allowedOrigins);
        
        // Solo métodos HTTP necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Headers específicos permitidos
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Cache-Control"
        ));
        
        // Headers expuestos
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        
        // Configuración de credenciales
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
