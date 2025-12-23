package org.meldtech.platform.features.continents.infra.r2dbc;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContinentRepository extends ReactiveCrudRepository<ContinentEntity, Long> {
    Mono<ContinentEntity> findByName(String name);
    Flux<ContinentEntity> findAllBy(Pageable pageable);
}
