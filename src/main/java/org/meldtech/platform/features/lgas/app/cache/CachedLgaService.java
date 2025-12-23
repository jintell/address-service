package org.meldtech.platform.features.lgas.app.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.meldtech.platform.features.lgas.api.LgaDto;
import org.meldtech.platform.features.lgas.app.LgaService;
import org.meldtech.platform.shared.cache.RedisReactiveCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;

import static org.meldtech.platform.shared.config.RedisConfig.convert;

@Service
public class CachedLgaService {
    private static final Logger log = LoggerFactory.getLogger(CachedLgaService.class);
    private final LgaService delegate;
    private final RedisReactiveCache<String> listCache;
    private final ObjectMapper objectMapper;

    private static final Duration LGA_LIST_CACHE_TTL = Duration.ofHours(24); // Countries rarely change


    // Build more specific cache keys based on pagination parameters
    private static final String CACHE_KEY_BY_STATE = "lga:state:code:%s:page:%d:size:%d";
    private static final String CACHE_KEY_BY_NAME = "lga:name:%s:page:%d:size:%d";

    public CachedLgaService(LgaService delegate,
                                RedisReactiveCache<String> listCache,
                                ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.listCache = listCache;
        this.objectMapper = objectMapper;
        this.listCache.setCachePrefix("addressCache:");
    }

    public Flux<LgaDto.LgaResponse> listByStateCode(String stateCode, int page, int size) {
        log.debug("Getting Lga by state code: {}", stateCode);
        String cacheKey = String.format(CACHE_KEY_BY_STATE, stateCode, page, size);

        return listCache.get(
                cacheKey,
                delegate.listByStateCode(stateCode, page, size).collectList().map(listCache::flatten),
                LGA_LIST_CACHE_TTL
        ).flatMapMany(this::rebuildResponse);
    }

    public Flux<LgaDto.LgaResponse> listByName(String name, int page, int size) {
        log.debug("Getting Lga by local government name: {}", name);
        String cacheKey = String.format(CACHE_KEY_BY_NAME, name, page, size);

        return listCache.get(
                cacheKey,
                delegate.listByName(name, page, size).collectList().map(listCache::flatten),
                LGA_LIST_CACHE_TTL
        ).flatMapMany(this::rebuildResponse);
    }

    private Flux<LgaDto.LgaResponse> rebuildResponse(String data) {
        return Flux.fromIterable(Objects.requireNonNull(convert(objectMapper, data, new TypeReference<>() {
        })));
    }
}
