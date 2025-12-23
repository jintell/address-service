package org.meldtech.platform.features.states.web;

import org.meldtech.platform.features.states.api.StateDto;
import org.meldtech.platform.features.states.app.StateService;
import org.meldtech.platform.features.states.app.cache.CachedStateService;
import org.meldtech.platform.shared.web.Pagination;
import org.meldtech.platform.shared.web.Responses;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class StateHandler {
    private final StateService service;
    private final CachedStateService cacheService;

    public StateHandler(StateService service, CachedStateService cacheService) {
        this.service = service;
        this.cacheService = cacheService;
    }

    public Mono<ServerResponse> addState(ServerRequest request) {
        return request.bodyToMono(StateDto.StateCreateRequest.class)
                .flatMap(service::create)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> updateState(ServerRequest request) {
        String code = request.pathVariable("code");
        return request.bodyToMono(StateDto.StateUpdateRequest.class)
                .flatMap(countryUpdateRequest ->  service.update(code, countryUpdateRequest))
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> listStates(ServerRequest request) {
        var p = Pagination.from(request);
        return Responses.paginated(request, service.getAll(p.page(), p.size()), service.count());
    }

    public Mono<ServerResponse> listStatesByCountryCode(ServerRequest request) {
        String countryCode = request.pathVariable("countryCode");
        var p = Pagination.from(request);
        return Responses.paginated(request, cacheService.getByCountryCode(countryCode, p.page(), p.size()), service.countByCountryCode(countryCode));
    }

    public Mono<ServerResponse> listStatesByName(ServerRequest request) {
        String name = request.pathVariable("name");
        var p = Pagination.from(request);
        return Responses.paginated(request, cacheService.getByName(name, p.page(), p.size()), service.countByName(name));
    }
}
