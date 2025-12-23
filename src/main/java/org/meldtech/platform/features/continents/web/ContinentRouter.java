package org.meldtech.platform.features.continents.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class ContinentRouter {
    @Bean
    RouterFunction<ServerResponse> continentsRoutes(ContinentHandler handler) {
        String base = "/v1/location/continents";

        return RouterFunctions.route()
                .POST(base, accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::addContinent)
                .GET(base, handler::listContinents)
                .PATCH(base + "/{name}", accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::updateContinent)
                .build();
    }
}
