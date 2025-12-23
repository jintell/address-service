package org.meldtech.platform.features.continents.web;

import org.meldtech.platform.features.continents.api.ContinentDto;
import org.meldtech.platform.features.continents.app.ContinentService;
import org.meldtech.platform.shared.web.Pagination;
import org.meldtech.platform.shared.web.Responses;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ContinentHandler {
    private final ContinentService service;
    
    public ContinentHandler(ContinentService service) {
        this.service = service;
    }

    public Mono<ServerResponse> addContinent(ServerRequest request) {
        return request.bodyToMono(ContinentDto.ContinentRequest.class)
                .flatMap(service::create)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> updateContinent(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(ContinentDto.ContinentRequest.class)
                .flatMap(continentUpdateRequest ->  service.update(name, continentUpdateRequest))
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> listContinents(ServerRequest request) {
        var p = Pagination.from(request);
        return Responses.paginated(request, service.getAll(p.page(), p.size()), service.count());
    }
}
