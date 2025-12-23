package org.meldtech.platform.features.states.infra.r2dbc;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StateRepository extends ReactiveCrudRepository<StateEntity, Long> {
    Mono<StateEntity> findByCode(String code);
    Flux<StateEntity> findByCountryCode(String countryCode, Pageable pageable);
    Flux<StateEntity> findByName(String name, Pageable pageable);
    Flux<StateEntity> findAllBy(Pageable pageable);
    Mono<Long> countAllByCountryCode(String countryCode);
    Mono<Long> countAllByName(String name);
}
