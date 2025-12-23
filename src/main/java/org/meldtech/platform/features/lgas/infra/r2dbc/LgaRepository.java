package org.meldtech.platform.features.lgas.infra.r2dbc;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LgaRepository extends ReactiveCrudRepository<LgaEntity, Long> {
    @Query("SELECT * FROM local_government_areas WHERE state_code = :stateCode ORDER BY id LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}")
    Flux<LgaEntity> findByStateCode(String stateCode, Pageable pageable);
    Flux<LgaEntity> findByName(String name, Pageable pageable);
    Mono<LgaEntity> findBySlugCode(String code);
    Mono<Long> countAllByStateCode(String stateCode);
    Mono<Long> countAllByName(String name);
}
