package org.meldtech.platform.features.lgas.web;

import org.meldtech.platform.features.lgas.api.LgaDto;
import org.meldtech.platform.features.lgas.app.LgaService;
import org.meldtech.platform.features.lgas.app.cache.CachedLgaService;
import org.meldtech.platform.shared.web.Pagination;
import org.meldtech.platform.shared.web.Responses;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class LgaHandler {
    private final LgaService service;
    private final CachedLgaService cacheService;
    
    public LgaHandler(LgaService service, CachedLgaService cacheService) {
        this.service = service;
        this.cacheService = cacheService;
    }

    public Mono<ServerResponse> addLga(ServerRequest request) {
        return request.bodyToMono(LgaDto.LgaCreateRequest.class)
                .flatMap(service::create)
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> updateLga(ServerRequest request) {
        String code = request.pathVariable("slugCode");
        return request.bodyToMono(LgaDto.LgaUpdateRequest.class)
                .flatMap(lgaUpdateRequest ->  service.update(code, lgaUpdateRequest))
                .flatMap(Responses::ok);
    }

    public Mono<ServerResponse> listLgaByStateCode(ServerRequest request) {
        String stateCode = request.pathVariable("stateCode");
        var p = Pagination.from(request);
        return Responses.paginated(request, cacheService.listByStateCode(stateCode, p.page(), p.size()), service.count());
    }

    public Mono<ServerResponse> listLgaByName(ServerRequest request) {
        String name = request.pathVariable("name");
        var p = Pagination.from(request);
        return Responses.paginated(request, cacheService.listByName(name, p.page(), p.size()), service.count());
    }
}
