package org.meldtech.platform.features.countries.app.cache;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.meldtech.platform.features.countries.api.CountryDto;
import org.meldtech.platform.features.countries.app.CountryService;
import org.meldtech.platform.shared.cache.RedisReactiveCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

import static org.meldtech.platform.shared.config.RedisConfig.convert;

@Service
public class CachedCountryService {
    private static final Logger log = LoggerFactory.getLogger(CachedCountryService.class);
    private final RedisReactiveCache<String> listCache;
    private final RedisReactiveCache<CountryDto.CountryResponse> appCache;
    private final ObjectMapper objectMapper;

    private static final Duration COUNTRY_CACHE_TTL = Duration.ofDays(7); // Country rarely changes
    private static final Duration COUNTRY_LIST_CACHE_TTL = Duration.ofHours(24); // Countries rarely change

    private final CountryService delegate;

    // Build more specific cache keys based on pagination parameters
    private static final String CACHE_KEY_BY_CONTINENT = "country:continent:%d:page:%d:size:%d";
    private static final String CACHE_KEY_BY_ISO2 = "country:iso2:%s";
    private static final String CACHE_KEY_BY_ISO3= "country:iso3:%s";

    public CachedCountryService(CountryService delegate,
                                RedisReactiveCache<String> listCache,
                                RedisReactiveCache<CountryDto.CountryResponse> appCache,
                                ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.listCache = listCache;
        this.appCache = appCache;
        this.objectMapper = objectMapper;
        this.appCache.setCachePrefix("addressCache:");
        this.listCache.setCachePrefix("addressCache:");
    }

    public Mono<CountryDto.CountryResponse> getByIsoAlpha2(String code) {
        log.debug("Getting Country by iso alpha 2 code: {}", code);
        String normalizedCode = code != null ? code.toUpperCase() : null;
        String cacheKey = String.format(CACHE_KEY_BY_ISO2, code);

        return appCache.get(
                cacheKey,
                delegate.getByIsoAlpha2(normalizedCode),
                COUNTRY_CACHE_TTL,
                CountryDto.CountryResponse.class
        );
    }

    public Mono<CountryDto.CountryResponse> getByIsoAlpha3(String code) {
        log.debug("Getting Country by iso alpha 3 code: {}", code);
        String cacheKey = String.format(CACHE_KEY_BY_ISO3, code);

        return appCache.get(
                cacheKey,
                delegate.getByIsoAlpha3(code),
                COUNTRY_CACHE_TTL,
                CountryDto.CountryResponse.class
        );
    }

    public Flux<CountryDto.CountryResponse> getAllByContinentId(long continentId, int page, int size) {
        log.debug("Getting Country by content id: {}", continentId);
        String cacheKey = String.format(CACHE_KEY_BY_CONTINENT, continentId, page, size);

        return listCache.get(
                        cacheKey,
                        delegate.getAllByContinentId(continentId, page, size).collectList().map(listCache::flatten),
                        COUNTRY_LIST_CACHE_TTL
                ).flatMapMany(this::rebuildResponse);
    }

    private Flux<CountryDto.CountryResponse> rebuildResponse(String data) {
        log.debug("Rebuilding response: {}", data);
        return Flux.fromIterable(Objects.requireNonNull(convert(objectMapper, data, new TypeReference<>() {
        })));
    }

}
