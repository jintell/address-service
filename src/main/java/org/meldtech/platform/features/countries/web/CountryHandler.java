package org.meldtech.platform.features.countries.web;


import org.meldtech.platform.features.countries.api.CountryDto;
import org.meldtech.platform.features.countries.app.CountryService;
import org.meldtech.platform.features.countries.app.cache.CachedCountryService;
import org.meldtech.platform.shared.web.Pagination;
import org.meldtech.platform.shared.web.Responses;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class CountryHandler {
    private final CountryService service;
    private final CachedCountryService cacheService;
    
    public CountryHandler(CountryService service, CachedCountryService cacheService) {
        this.service = service;
        this.cacheService = cacheService;
    }

    public Mono<ServerResponse> addCountry(ServerRequest request) {
        return request.bodyToMono(CountryDto.CountryCreateRequest.class)
                .flatMap(service::create)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> updateCountry(ServerRequest request) {
        String code = request.pathVariable("isoAlpha2");
        return request.bodyToMono(CountryDto.CountryUpdateRequest.class)
                .flatMap(countryUpdateRequest ->  service.update(code, countryUpdateRequest))
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> listCountries(ServerRequest request) {
        long continentCode = Long.parseLong(request.pathVariable("continentCode"));
        var p = Pagination.from(request);
        return Responses.paginated(request, cacheService.getAllByContinentId(continentCode, p.page(), p.size()), service.countByContinentId(continentCode));
    }

    public Mono<ServerResponse> getIsoAlpha2(ServerRequest request) {
        String code = request.pathVariable("isoAlpha2");
        return cacheService.getByIsoAlpha2(code)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> getIsoAlpha3(ServerRequest request) {
        String code = request.pathVariable("isoAlpha3");
        return cacheService.getByIsoAlpha3(code)
                .flatMap(Responses::ok);
    }
}
