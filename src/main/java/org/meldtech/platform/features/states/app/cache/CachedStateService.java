package org.meldtech.platform.features.states.app.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.meldtech.platform.features.states.api.StateDto;
import org.meldtech.platform.features.states.app.StateService;
import org.meldtech.platform.shared.cache.RedisReactiveCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;

import static org.meldtech.platform.shared.config.RedisConfig.convert;

@Service
public class CachedStateService {
    private static final Logger log = LoggerFactory.getLogger(CachedStateService.class);
    private final StateService delegate;
    private final RedisReactiveCache<String> listCache;
    private final ObjectMapper objectMapper;

    private static final Duration STATE_LIST_CACHE_TTL = Duration.ofHours(24); // Countries rarely change

    // Build more specific cache keys based on pagination parameters
    private static final String CACHE_KEY_BY_COUNTRY = "state:country:code:%s:page:%d:size:%d";
    private static final String CACHE_KEY_BY_NAME = "state:country:name:%s:page:%d:size:%d";

    public CachedStateService(StateService delegate,
                                RedisReactiveCache<String> listCache,
                                ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.listCache = listCache;
        this.objectMapper = objectMapper;
        this.listCache.setCachePrefix("addressCache:");
    }

    public Flux<StateDto.StateResponse> getByCountryCode(String countryCode, int page, int size) {
        log.debug("Getting State by country code: {}", countryCode);
        String cacheKey = String.format(CACHE_KEY_BY_COUNTRY, countryCode, page, size);

        return listCache.get(
                cacheKey,
                delegate.getByCountryCode(countryCode, page, size).collectList().map(listCache::flatten),
                STATE_LIST_CACHE_TTL
        ).flatMapMany(this::rebuildResponse);
    }

    public Flux<StateDto.StateResponse> getByName(String name, int page, int size) {
        log.debug("Getting State by name: {}", name);
        String cacheKey = String.format(CACHE_KEY_BY_NAME, name, page, size);

        return listCache.get(
                cacheKey,
                delegate.getByName(name, page, size).collectList().map(listCache::flatten),
                STATE_LIST_CACHE_TTL
        ).flatMapMany(this::rebuildResponse);
    }

    private Flux<StateDto.StateResponse> rebuildResponse(String data) {
        log.debug("Rebuilding response: {}", data);
        return Flux.fromIterable(Objects.requireNonNull(convert(objectMapper, data, new TypeReference<>() {
        })));
    }

}

