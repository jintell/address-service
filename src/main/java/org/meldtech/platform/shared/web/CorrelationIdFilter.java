package org.meldtech.platform.shared.web;

import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Simple correlation-id filter. Adds/propagates X-Correlation-Id header and puts it to MDC.
 */
@Component
public class CorrelationIdFilter implements WebFilter {
    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String TENANT_HEADER_NAME = "X-Tenant-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String headerCid = req.getHeaders().getFirst(HEADER_NAME);
        String tenantId = req.getHeaders().getFirst(TENANT_HEADER_NAME);
        final String cid = (headerCid == null || headerCid.isBlank()) ? UUID.randomUUID().toString() : headerCid;
        ServerWebExchange ex = exchange;
        if (headerCid == null || headerCid.isBlank()) {
            ex = exchange.mutate()
                    .request(builder -> builder.header(HEADER_NAME, cid))
                    .build();
        }
        MDC.put(HEADER_NAME, cid);
        return chain.filter(ex)
                .doFinally(signal -> MDC.remove(HEADER_NAME))
                .contextWrite(ctx -> {
                    ctx.put(HEADER_NAME, cid);
                    ctx.put(TENANT_HEADER_NAME, tenantId == null ? "" : tenantId);
                    return ctx;
                });
    }
}
