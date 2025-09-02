package pe.edu.vallegrande.ms_distribution.application.config;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class SecurityHeadersFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().getHeaders().add(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff");
        exchange.getResponse().getHeaders().add(HttpHeaders.X_FRAME_OPTIONS, "DENY");
        exchange.getResponse().getHeaders().add(HttpHeaders.X_XSS_PROTECTION, "1; mode=block");
        exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
        exchange.getResponse().getHeaders().add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Remover headers que pueden revelar información
        exchange.getResponse().getHeaders().remove(HttpHeaders.SERVER);
        exchange.getResponse().getHeaders().remove("X-Powered-By");
        exchange.getResponse().getHeaders().remove("X-AspNet-Version");
        exchange.getResponse().getHeaders().remove("X-AspNetMvc-Version");
        
        return chain.filter(exchange);
    }
}
