package org.meldtech.platform.features.lgas.app;


import org.meldtech.platform.features.lgas.domain.Lga;
import org.meldtech.platform.features.lgas.infra.r2dbc.LgaEntity;
import org.meldtech.platform.features.lgas.infra.r2dbc.LgaRepository;
import org.meldtech.platform.shared.web.DatabaseException;
import org.meldtech.platform.shared.web.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.meldtech.platform.features.lgas.api.LgaDto.LgaCreateRequest;
import static org.meldtech.platform.features.lgas.api.LgaDto.LgaUpdateRequest;
import static org.meldtech.platform.features.lgas.api.LgaDto.LgaResponse;

@Service
public class LgaService {
    private static final Logger logger = LoggerFactory.getLogger(LgaService.class);
    private final LgaRepository lgaRepository;

    public LgaService(LgaRepository lgaRepository) {
        this.lgaRepository = lgaRepository;
    }

    public Mono<LgaResponse> create(LgaCreateRequest request) {
        return lgaRepository.save(toEntity(Lga.create(request.name(), request.slugCode(), request.latitude(), request.longitude(), request.stateCode(), request.countryCode())))
                .map(this::toResponse)
                .onErrorResume(e -> Mono.error(new DatabaseException(e.getMessage())));
    }

    public Mono<LgaResponse> update(String code, LgaUpdateRequest request) {
        return lgaRepository.findBySlugCode(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("lga not found")))
                .map(lga -> toDomain(lga, request))
                .flatMap(lga -> Mono.just(lga.update(lga, request.name(), request.slugCode(), request.latitude(), request.longitude(), request.stateCode(), request.countryCode())))
                .map(this::toEntity)
                .flatMap(lgaRepository::save)
                .map(this::toResponse);
    }

    public Mono<Long> count() {
        return lgaRepository.count();
    }

    public Flux<LgaResponse> listByStateCode(String stateCode, int page, int size) {
        logger.debug("Finding LGAs by stateCode: {}, page: {}, size: {}", stateCode, page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return lgaRepository.findByStateCode(stateCode, pageable)
                .map(this::toResponse);
    }

    public Flux<LgaResponse> listByName(String name, int page, int size) {
        logger.debug("Finding LGA by name: {}, page: {}, size: {}", name, page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return lgaRepository.findByName(name, pageable)
                .map(this::toResponse);
    }

    private LgaEntity toEntity(Lga lga) {
        return LgaEntity.builder()
                .id(lga.id())
                .name(lga.name())
                .slugCode(lga.slugCode())
                .latitude(lga.latitude())
                .longitude(lga.longitude())
                .stateCode(lga.stateCode())
                .countryCode(lga.countryCode())
                .build();
    }

    private Lga toDomain(LgaEntity entity, LgaUpdateRequest request) {
        return Lga.builder()
                .id(entity.id())
                .name(request.name())
                .slugCode(request.slugCode())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .stateCode(request.stateCode())
                .countryCode(request.countryCode())
                .build();
    }

    private LgaResponse toResponse(LgaEntity entity) {
        return new LgaResponse(
                entity.name(),
                entity.slugCode(),
                entity.latitude(),
                entity.longitude(),
                entity.stateCode(),
                entity.countryCode(),
                entity.createdOn()
                 );
    }
}
