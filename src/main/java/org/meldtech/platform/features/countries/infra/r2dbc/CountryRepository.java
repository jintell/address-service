package org.meldtech.platform.features.countries.infra.r2dbc;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CountryRepository extends ReactiveCrudRepository<CountryEntity, Long> {
    Flux<CountryEntity> findAllBy(Pageable pageable);
    Mono<CountryEntity> findByIsoAlpha2(String code);
    Mono<CountryEntity> findByIsoAlpha3(String code);
    Flux<CountryEntity> findByContinentId(Long continentId, Pageable pageable);
    Mono<Long> countAllByContinentId(Long continentId);
}
