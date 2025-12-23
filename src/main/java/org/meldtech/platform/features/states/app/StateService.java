package org.meldtech.platform.features.states.app;

import org.meldtech.platform.features.states.domain.State;
import org.meldtech.platform.features.states.infra.r2dbc.StateEntity;
import org.meldtech.platform.features.states.infra.r2dbc.StateRepository;
import org.meldtech.platform.shared.web.DatabaseException;
import org.meldtech.platform.shared.web.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.meldtech.platform.features.states.api.StateDto.StateCreateRequest;
import static org.meldtech.platform.features.states.api.StateDto.StateUpdateRequest;
import static org.meldtech.platform.features.states.api.StateDto.StateResponse;

@Service
public class StateService {
    private static final Logger logger = LoggerFactory.getLogger(StateService.class);
    private final StateRepository stateRepository;

    public StateService(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public Mono<StateResponse> create(StateCreateRequest request) {
        return stateRepository.save(toEntity(State.create(request.name(), request.code(), request.postalCode(), request.countryCode())))
                .map(this::toResponse)
                .onErrorResume(e -> Mono.error(new DatabaseException(e.getMessage())));
    }

    public Mono<StateResponse> update(String code, StateUpdateRequest request) {
        return stateRepository.findByCode(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("state not found")))
                .map(state -> toDomain(state, request))
                .flatMap(state -> Mono.just(state.update(state, request.name(), request.code(), request.postalCode(), request.countryCode())))
                .map(this::toEntity)
                .flatMap(stateRepository::save)
                .map(this::toResponse);
    }

    public Flux<StateResponse> getAll(int page, int size) {
        logger.debug("Finding states with: page: {}, size: {}", page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return stateRepository.findAllBy(pageable)
                .map(this::toResponse);
    }

    public Mono<Long> count() {
        return stateRepository.count();
    }

    public Flux<StateResponse> getByCountryCode(String countryCode, int page, int size) {
        logger.debug("Finding states by country code: {}, page: {}, size: {}", countryCode, page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return stateRepository.findByCountryCode(countryCode, pageable)
                .map(this::toResponse);
    }

    public Flux<StateResponse> getByName(String name, int page, int size) {
        logger.debug("Finding states by name: {}, page: {}, size: {}", name, page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s);

        return stateRepository.findByName(name, pageable)
                .map(this::toResponse);
    }

    public Mono<Long> countByCountryCode(String countryCode) {
        return stateRepository.countAllByCountryCode(countryCode);
    }

    public Mono<Long> countByName(String name) {
        return stateRepository.countAllByName(name);
    }

    private StateEntity toEntity(State state) {
        return StateEntity.builder()
                .id(state.id())
                .name(state.name())
                .code(state.code())
                .postalCode(state.postalCode())
                .countryCode(state.countryCode())
                .build();
    }

    private State toDomain(StateEntity entity) {
        return State.builder()
                .id(entity.id())
                .name(entity.name())
                .code(entity.code())
                .postalCode(entity.postalCode())
                .countryCode(entity.countryCode())
                .build();
    }

    private State toDomain(StateEntity entity, StateUpdateRequest request) {
        return State.builder()
                .id(entity.id())
                .name(request.name())
                .code(request.code())
                .postalCode(request.postalCode())
                .countryCode(request.countryCode())
                .build();
    }

    private StateResponse toResponse(StateEntity entity) {
        return new StateResponse(
                entity.name(),
                entity.code(),
                entity.postalCode(),
                entity.countryCode(),
                entity.createdOn());
    }
}
