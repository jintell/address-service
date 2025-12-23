package org.meldtech.platform.features.geocoding.app.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.meldtech.platform.features.geocoding.api.GeoCodingDto;
import org.meldtech.platform.features.geocoding.app.GeoCodeService;
import org.meldtech.platform.shared.cache.RedisReactiveCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static org.meldtech.platform.shared.config.RedisConfig.convert;

@Service
public class CachedGeoCodeService {
    private static final Logger log = LoggerFactory.getLogger(CachedGeoCodeService.class);
    private final GeoCodeService delegate;
    private final RedisReactiveCache<String> listCache;
    private final ObjectMapper objectMapper;

    private static final Duration GEO_CODE_CACHE_TTL = Duration.ofDays(7); // Countries rarely change

    // Build more specific cache keys based on pagination parameters
    private static final String CACHE_KEY_BY_ADDRESS= "geoCode:address:%s";
    private static final String CACHE_KEY_BY_COORDINATES= "geoCode:coordinates:%f-%f";
    private static final String CACHE_KEY_BY_DISTANCE= "geoCode:distance:%f-%f-%f-%f";
    private static final String CACHE_KEY_BY_DISTANCE_IN_ADDRESS= "geoCode:distance:address:%s-%s";

    public CachedGeoCodeService(GeoCodeService delegate,
                              RedisReactiveCache<String> listCache,
                              ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.listCache = listCache;
        this.objectMapper = objectMapper;
        this.listCache.setCachePrefix("addressCache:");
    }

    public Mono<GeoCodingDto.GeoCodingResponse> geoCode(String address) {
        log.debug("Getting GeoCode address: {}", address);
        String cacheKey = String.format(CACHE_KEY_BY_ADDRESS, address.replace(" ", ""));

        return listCache.get(
                cacheKey,
                delegate.geoCode(address).map(listCache::flatten),
                GEO_CODE_CACHE_TTL
        ).flatMap(this::rebuildResponse);
    }

    public Mono<Map> getReverseGeoCode(double lat, double lon) {
        log.debug("Getting Reverse GeoCode by lat: {} and long: {}", lat, lon);
        String cacheKey = String.format(CACHE_KEY_BY_COORDINATES, lat, lon);

        return listCache.get(
                cacheKey,
                delegate.getReverseGeoCode(lat, lon).map(listCache::flatten),
                GEO_CODE_CACHE_TTL
        ).flatMap(this::rebuildMap);
    }

    public Mono<Double> getDistance(double lat1, double lon1, double lat2, double lon2) {
        log.debug("Getting GeoCode distance: co-ord 1 {}-{}  co-ord 2 {}-{}", lat1, lon1, lat2, lon2);
        String cacheKey = String.format(CACHE_KEY_BY_DISTANCE, lat1, lon1, lat2, lon2);

        return listCache.get(
                cacheKey,
                delegate.getDistance(lat1, lon1, lat2, lon2).map(listCache::flatten),
                GEO_CODE_CACHE_TTL
        ).map(Double::parseDouble);
    }

    public Mono<Double> getDistanceBetween(String address1, String address2) {
        log.debug("Getting distance by address: {} and address: {}", address1, address2);
        String cacheKey = String.format(CACHE_KEY_BY_DISTANCE_IN_ADDRESS, address1.replace(" ", ""), address2.replace(" ", ""));

        return listCache.get(
                cacheKey,
                delegate.getDistanceBetween(address1, address2).map(listCache::flatten),
                GEO_CODE_CACHE_TTL
        ).map(Double::parseDouble);
    }

    private Mono<GeoCodingDto.GeoCodingResponse> rebuildResponse(String data) {
        return Mono.just(Objects.requireNonNull(convert(objectMapper, data, GeoCodingDto.GeoCodingResponse.class)));
    }

    private Mono<Map> rebuildMap(String data) {
        return Mono.just(Objects.requireNonNull(convert(objectMapper, data, Map.class)));
    }
}
