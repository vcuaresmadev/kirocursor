package pe.edu.vallegrande.ms_distribution.application.config;

import io.github.bucket4j.Bucket;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(2)
public class RateLimitFilter implements WebFilter {

    private final Bucket rateLimitBucket;

    public RateLimitFilter(Bucket rateLimitBucket) {
        this.rateLimitBucket = rateLimitBucket;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Solo aplicar rate limiting a endpoints de la API
        if (exchange.getRequest().getPath().value().startsWith("/api/")) {
            if (rateLimitBucket.tryConsume(1)) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                
                String errorMessage = "{\"error\":\"Rate limit exceeded. Please try again later.\",\"retryAfter\":60}";
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes(StandardCharsets.UTF_8)))
                );
            }
        }
        
        return chain.filter(exchange);
    }
}
