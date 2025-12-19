package org.meldtech.platform.shared.web;

import reactor.core.publisher.Mono;

/**
 * Abstraction for deciding if a request identified by a key is allowed right now.
 */
public interface RateLimiter {
    /**
     * @param key unique identifier for the subject (e.g., ip/device/path/tenant composite)
     * @return Mono emitting true if allowed, false if limited
     */
    Mono<Boolean> allow(String key);
}
