package org.meldtech.platform.features.countries.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class CountryRouter {
    @Bean
    RouterFunction<ServerResponse> countryRoutes(CountryHandler handler) {
        String base = "/v1/location/countries";

        return RouterFunctions.route()
                .POST(base, accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::addCountry)
                .PATCH(base + "/{isoAlpha2}", accept(MediaType.APPLICATION_JSON)
                        .and(contentType(MediaType.APPLICATION_JSON)), handler::updateCountry)
                .GET(base, handler::list)
                .GET(base + "/{continentCode}", handler::listCountries)
                .GET(base + "/iso-2/{isoAlpha2}", handler::getIsoAlpha2)
                .GET(base + "/iso-3/{isoAlpha3}", handler::getIsoAlpha3)
                .build();
    }
}
