package org.meldtech.platform.shared.web;

import org.meldtech.platform.shared.config.RateLimitProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Redis-backed rate-limiting filter delegating to {@link RateLimiter}.
 * Controlled by {@link RateLimitProperties}.
 */
@Component
public class RateLimitingFilter implements WebFilter {

    private final RateLimitProperties props;
    private final RateLimiter limiter;

    public RateLimitingFilter(RateLimitProperties props, RateLimiter limiter) {
        this.props = props;
        this.limiter = limiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (props == null || !props.isEnabled()) return chain.filter(exchange);

        String key = buildKey(exchange);
        return limiter.allow(key)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    }
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    response.getHeaders().set("Content-Type", "application/problem+json");
                    String body = "{\"title\":\"rate_limited\",\"status\":429,\"detail\":\"Rate limit exceeded\"}";
                    var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer));
                });
    }

    private String buildKey(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        String subject = switch (props.getKeyStrategy()) {
            case DEVICE -> resolveDeviceId(request);
            case IP -> resolveIp(request);
            case IP_OR_DEVICE -> {
                String device = resolveDeviceId(request);
                yield (device != null && !device.isBlank()) ? device : resolveIp(request);
            }
        };
        if (subject == null || subject.isBlank()) subject = "unknown";
        String path = props.isPerPath() ? request.getPath().pathWithinApplication().value() : "*";
        return String.join(":", subject, path);
    }

    private String resolveDeviceId(ServerHttpRequest request) {
        for (String h : props.getDeviceHeaders()) {
            String v = request.getHeaders().getFirst(h);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private String resolveIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
