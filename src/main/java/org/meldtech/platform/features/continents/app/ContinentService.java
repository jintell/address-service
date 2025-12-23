package org.meldtech.platform.features.continents.app;

import org.meldtech.platform.features.continents.domain.Continent;
import org.meldtech.platform.features.continents.infra.r2dbc.ContinentEntity;
import org.meldtech.platform.features.continents.infra.r2dbc.ContinentRepository;
import org.meldtech.platform.shared.web.DatabaseException;
import org.meldtech.platform.shared.web.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.meldtech.platform.features.continents.api.ContinentDto.*;

@Service
public class ContinentService {
    private static final Logger logger = LoggerFactory.getLogger(ContinentService.class);
    private final ContinentRepository continentRepository;

    public ContinentService(ContinentRepository continentRepository) {
        this.continentRepository = continentRepository;
    }

    public Mono<ContinentResponse> create(ContinentRequest request) {
        return continentRepository.save(toEntity(Continent.create(request.name())))
                .map(this::toResponse)
                .onErrorResume(e -> Mono.error(new DatabaseException(e.getMessage())));
    }

    public Mono<ContinentResponse> update(String name, ContinentRequest request) {
        return continentRepository.findByName(name)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("continent not found")))
                .map(continent -> toDomain(continent, request))
                .flatMap(continent -> Mono.just(continent.update(continent, request.name())))
                .map(this::toEntity)
                .flatMap(continentRepository::save)
                .map(this::toResponse);
    }

    public Flux<ContinentResponse> getAll(int page, int size) {
        logger.debug("Finding continents with: page: {}, size: {}", page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return continentRepository.findAllBy(pageable)
                .map(this::toResponse);
    }

    public Mono<Long> count() {
        return continentRepository.count();
    }

    private ContinentEntity toEntity(Continent continent) {
        return ContinentEntity.builder()
                .id(continent.id())
                .name(continent.name())
                .build();
    }

    private Continent toDomain(ContinentEntity entity) {
        return Continent.builder()
                .id(entity.id())
                .name(entity.name())
                .build();
    }

    private Continent toDomain(ContinentEntity entity, ContinentRequest request) {
        return Continent.builder()
                .id(entity.id())
                .name(request.name())
                .build();
    }

    private ContinentResponse toResponse(ContinentEntity entity) {
        return new ContinentResponse(
                entity.name(),
                entity.createdOn() );
    }
}
