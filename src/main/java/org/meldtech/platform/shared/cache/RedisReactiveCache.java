package org.meldtech.platform.shared.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

import static org.meldtech.platform.shared.config.RedisConfig.convert;

@Component
public class RedisReactiveCache<T> {
    private static final Logger log = LoggerFactory.getLogger(RedisReactiveCache.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final ReactiveRedisTemplate<String, T> redisTemplate;
    private final ReactiveValueOperations<String, T> valueOps;
    private final ObjectMapper objectMapper;
    @Setter
    private String cachePrefix;

    /**
     * Creates a new RedisReactiveCache.
     *
     * @param redisTemplate the ReactiveRedisTemplate to use
     */
    public RedisReactiveCache(ReactiveRedisTemplate<String, T> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.objectMapper = objectMapper;
    }

    private static void error(Throwable e) {
        log.error("Error retrieving from cache: {}", e.getMessage());
        // Add more specific error handling for read-only replica issues
        if (e.getMessage() != null && e.getMessage().contains("READONLY")) {
            log.error("Redis is in read-only mode. This usually indicates you're connected to a replica instead of a master.");
        }
    }

    public Mono<T> get(String key, Mono<T> retriever) {
        return get(key, retriever, DEFAULT_TTL);
    }

    public Mono<T> get(String key, Mono<T> retriever, Duration ttl) {
        String cacheKey = getCacheKey(key);

        return valueOps.get(cacheKey)
                .switchIfEmpty(
                        retriever.flatMap(value -> {
                            if (value == null) {
                                return Mono.empty();
                            }
                            return put(key, value, ttl);
                        })
                )
                .doOnError(RedisReactiveCache::error);
    }

    public Mono<T> get(String key, Mono<T> retriever, Duration ttl, Class<T> type) {
        String cacheKey = getCacheKey(key);

        return valueOps.get(cacheKey)
                .switchIfEmpty(
                        retriever.flatMap(value -> {
                            if (value == null) {
                                return Mono.empty();
                            }
                            return put(key, (value), ttl);
                        })
                )
                .mapNotNull(t -> convert(objectMapper, Objects.requireNonNull(convert(objectMapper, t, String.class)), type))
                .doOnError(RedisReactiveCache::error);
    }

    public Mono<T> put(String key, T value) {
        return put(key, value, DEFAULT_TTL);
    }

    public Mono<T> put(String key, T value, Duration ttl) {
        String cacheKey = getCacheKey(key);

        return valueOps.set(cacheKey, value, ttl)
                .thenReturn(value);
    }

    public Mono<Void> remove(String key) {
        String cacheKey = getCacheKey(key);

        return redisTemplate.delete(cacheKey)
                .then()
                .doOnError(e -> log.error("Error removing from cache: {}", e.getMessage()));
    }

    public Mono<Void> removeByPattern(String keyPattern) {
        String pattern = getCacheKey(keyPattern + "*");

        return redisTemplate.keys(pattern)
                .flatMap(redisTemplate::delete)
                .then()
                .doOnError(e -> log.error("Error removing by pattern from cache: {}", e.getMessage()));
    }

    /**
     * Gets the full cache key with a prefix.
     *
     * @param key the key
     * @return the full cache key
     */
    private String getCacheKey(String key) {
        return cachePrefix + ":" + key;
    }

    /**
     * Serializes this response to JSON for caching.
     */
    public String flatten(Object t) {
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize PaginatedResponse", e);
        }
    }
}
