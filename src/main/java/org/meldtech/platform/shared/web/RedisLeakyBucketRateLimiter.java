package org.meldtech.platform.shared.web;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Redis-backed leaky bucket rate limiter.
 * Redis key stores two numbers separated by a colon: "tokens:lastMs".
 * The script leaks tokens based on elapsed time and increments by 1 for the current request.
 * If capacity is exceeded, returns 0 (blocked), else stores a new state and returns 1 (allowed).
 */
public class RedisLeakyBucketRateLimiter implements RateLimiter {

    private final ReactiveStringRedisTemplate redis;
    private final String keyPrefix;
    private final int capacity;
    private final double leakPerSecond;
    private final DefaultRedisScript<Long> script;
    private final Duration ttl;

    private static final double MIN_LEAK_RATE = 0.001;
    private static final long BUFFER_SECONDS = 30;
    private static final long MIN_TTL_SECONDS = 60;

    public RedisLeakyBucketRateLimiter(ReactiveStringRedisTemplate redis,
                                       String keyPrefix,
                                       int capacity,
                                       double leakPerSecond) {
        this.redis = redis;
        this.keyPrefix = keyPrefix;
        this.capacity = capacity;
        this.leakPerSecond = leakPerSecond;
        this.script = new DefaultRedisScript<>();
        this.script.setResultType(Long.class);
        this.script.setScriptText(LUA);
        double effectiveLeakRate = Math.max(MIN_LEAK_RATE, leakPerSecond);
        // TTL to auto-clean inactive buckets: roughly capacity/leak seconds plus a buffer
        long seconds = Math.max(MIN_TTL_SECONDS, (long) Math.ceil(capacity / effectiveLeakRate + BUFFER_SECONDS));
        this.ttl = Duration.ofSeconds(seconds);
    }

    @Override
    public Mono<Boolean> allow(String key) {
        if (key == null || key.isBlank()) return Mono.just(true);
        String redisKey = keyPrefix + ":" + key;
        System.err.println("redis key: " + redisKey + "");
        long nowMs = Instant.now().toEpochMilli();
        String capacityStr = Integer.toString(capacity);
        String leakPerSecStr = Double.toString(leakPerSecond);
        String nowMsStr = Long.toString(nowMs);
        String ttlSeconds = Long.toString(ttl.toSeconds());
        return redis.execute(this.script, List.of(redisKey), nowMsStr, capacityStr, leakPerSecStr, ttlSeconds)
                .single()
                .map(result -> result == 1L)
                .onErrorReturn(true); // fail open to avoid hard outages if Redis blips
    }

    private static final String LUA = """
            local key = KEYS[1]
            local nowMs = tonumber(ARGV[1])
            local capacity = tonumber(ARGV[2])
            local leakPerSec = tonumber(ARGV[3])
            local ttlSec = tonumber(ARGV[4])

            local state = redis.call('GET', key)
            local tokens = 0
            local lastMs = nowMs
            if state then
                local sep = string.find(state, ':')
                if sep then
                    tokens = tonumber(string.sub(state, 1, sep - 1)) or 0
                    lastMs = tonumber(string.sub(state, sep + 1)) or nowMs
                end
            end
            local elapsed = math.max(0, nowMs - lastMs)
            local leaked = (elapsed / 1000.0) * leakPerSec
            tokens = math.max(0, tokens - leaked)
            local newTokens = tokens + 1
            if newTokens > capacity then
                -- blocked
                return 0
            else
                -- store new state and allow
                local newState = tostring(newTokens) .. ':' .. tostring(nowMs)
                redis.call('SETEX', key, ttlSec, newState)
                return 1
            end
        """;
}
