package org.meldtech.platform.shared.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();
        exchange.getRequest().getMethod();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        return chain.filter(exchange)
                .doOnSuccess(v -> {
                    ServerHttpResponse res = exchange.getResponse();
                    long took = System.currentTimeMillis() - start;
                    int status = res.getStatusCode() != null ? res.getStatusCode().value() : 200;
                    log.info("{} {} -> {} ({} ms)", method, path, status, took);
                })
                .doOnError(ex -> {
                    ServerHttpResponse res = exchange.getResponse();
                    long took = System.currentTimeMillis() - start;
                    int status = res.getStatusCode() != null ? res.getStatusCode().value() : 500;
                    log.warn("{} {} -> {} ({} ms) ex={}", method, path, status, took, ex.toString());
                });
    }
}
