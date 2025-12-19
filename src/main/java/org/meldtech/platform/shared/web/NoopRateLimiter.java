package org.meldtech.platform.shared.web;

import reactor.core.publisher.Mono;

public class NoopRateLimiter implements RateLimiter {
    @Override
    public Mono<Boolean> allow(String key) {
        return Mono.just(true);
    }
}
