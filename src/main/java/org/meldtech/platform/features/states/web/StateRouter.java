package org.meldtech.platform.features.states.web;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class StateRouter {
    @Bean
    RouterFunction<ServerResponse> stateRoutes(StateHandler handler) {
        String base = "/v1/location/states";

        return RouterFunctions.route()
                .POST(base, accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::addState)
                .PATCH(base + "/{code}", accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::updateState)
                .GET(base, handler::listStates)
                .GET(base + "/country/{countryCode}", handler::listStatesByCountryCode)
                .GET(base + "/name/{name}", handler::listStatesByName)
                .build();
    }
}
