package org.meldtech.platform.features.geocoding.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class GeoCodeRouter {
    @Bean
    RouterFunction<ServerResponse> geoCodeRoutes(GeoCodeHandler handler) {
        String base = "/v1/location/geocodes";

        return RouterFunctions.route()
                .GET(base + "/coordinates", handler::getCoordinate)
                .GET(base + "/address", handler::getAddress)
                .GET(base + "/distance", handler::getCoordinatesDistance)
                .GET(base + "/address/distance", handler::getDistanceFromAddresses)
                .build();
    }
}
