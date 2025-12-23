package org.meldtech.platform.features.geocoding.web;

import org.meldtech.platform.features.geocoding.app.GeoCodeService;
import org.meldtech.platform.features.geocoding.app.cache.CachedGeoCodeService;
import org.meldtech.platform.features.lgas.api.LgaDto;
import org.meldtech.platform.shared.web.Responses;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class GeoCodeHandler {
    private final CachedGeoCodeService service;

    public GeoCodeHandler(CachedGeoCodeService service) {
        this.service = service;
    }

    public Mono<ServerResponse> getCoordinate(ServerRequest request) {
        String address = request.queryParams().getFirst("address");
        return service.geoCode(address)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> getAddress(ServerRequest request) {
        double latitude = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("latitude")));
        double longitude = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("longitude")));
        return service.getReverseGeoCode(latitude, longitude)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> getCoordinatesDistance(ServerRequest request) {
        double latitude = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("latitude")));
        double longitude = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("longitude")));
        double latitude2 = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("latitude1")));
        double longitude2 = Double.parseDouble(Objects.requireNonNull(request.queryParams().getFirst("longitude1")));
        return service.getDistance(latitude, longitude, latitude2, longitude2)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> getDistanceFromAddresses(ServerRequest request) {
        String address1 = request.queryParams().getFirst("address");
        String address2 = request.queryParams().getFirst("to-address");
        return service.getDistanceBetween(address1, address2)
                .flatMap(Responses::ok);
    }
}
