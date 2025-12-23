package org.meldtech.platform.features.lgas.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class LgaRouter {
    @Bean
    RouterFunction<ServerResponse> lgaRoutes(LgaHandler handler) {
        String base = "/v1/location/lgas";

        return RouterFunctions.route()
                .POST(base, accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::addLga)
                .PATCH(base + "/{slugCode}", accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::updateLga)
                .GET(base + "/states/{stateCode}", handler::listLgaByStateCode)
                .GET(base + "/name/{name}", handler::listLgaByName)
                .build();
    }
}
