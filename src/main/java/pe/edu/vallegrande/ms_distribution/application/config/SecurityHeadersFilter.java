package pe.edu.vallegrande.ms_distribution.application.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class SecurityHeadersFilter implements WebFilter {

    // Definimos constantes personalizadas para mayor claridad
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String REFERRER_POLICY = "Referrer-Policy";
    private static final String PERMISSIONS_POLICY = "Permissions-Policy";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Cabeceras de seguridad recomendadas por OWASP
        exchange.getResponse().getHeaders().add(X_CONTENT_TYPE_OPTIONS, "nosniff");
        exchange.getResponse().getHeaders().add(X_FRAME_OPTIONS, "DENY");
        exchange.getResponse().getHeaders().add(X_XSS_PROTECTION, "1; mode=block");
        exchange.getResponse().getHeaders().add(REFERRER_POLICY, "strict-origin-when-cross-origin");
        exchange.getResponse().getHeaders().add(PERMISSIONS_POLICY, "geolocation=(), microphone=(), camera=()");
        exchange.getResponse().getHeaders().add(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");

        // Remover headers que podrían filtrar información sensible
        exchange.getResponse().getHeaders().remove("Server");
        exchange.getResponse().getHeaders().remove("X-Powered-By");
        exchange.getResponse().getHeaders().remove("X-AspNet-Version");
        exchange.getResponse().getHeaders().remove("X-AspNetMvc-Version");

        return chain.filter(exchange);
    }
}
