package org.meldtech.platform.features.countries.app;

import org.meldtech.platform.features.countries.domain.Country;
import org.meldtech.platform.features.countries.infra.r2dbc.CountryEntity;
import org.meldtech.platform.features.countries.infra.r2dbc.CountryRepository;
import org.meldtech.platform.shared.web.DatabaseException;
import org.meldtech.platform.shared.web.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.meldtech.platform.features.countries.api.CountryDto.CountryCreateRequest;
import static org.meldtech.platform.features.countries.api.CountryDto.CountryUpdateRequest;
import static org.meldtech.platform.features.countries.api.CountryDto.CountryResponse;

@Service
public class CountryService {
    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);
    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public Mono<CountryResponse> create(CountryCreateRequest request) {
        return countryRepository.save(toEntity(Country.create(request.name(), request.isoAlpha2(), request.isoAlpha3(), request.capital(), request.dialingCode(), request.flag(), request.continentCode())))
                .map(this::toResponse)
                .onErrorResume(e -> Mono.error(new DatabaseException(e.getMessage())));
    }

    public Mono<CountryResponse> update(String code, CountryUpdateRequest request) {
        return countryRepository.findByIsoAlpha2(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("country not found")))
                .map(country -> toDomain(country, request))
                .flatMap(country -> Mono.just(country.update(country, request.name(), request.isoAlpha2(), request.isoAlpha3(), request.capital(), request.dialingCode(), request.flag(), request.continentCode())))
                .map(this::toEntity)
                .flatMap(countryRepository::save)
                .map(this::toResponse);
    }

    public Flux<CountryResponse> getAllByContinentId(long continentId, int page, int size) {
        logger.debug("Finding countries by continent id with: page: {}, size: {}", page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s, Sort.by("name").ascending());

        return countryRepository.findByContinentId(continentId, pageable)
                .map(this::toResponse);
    }

    public Flux<CountryResponse> getAll(int page, int size) {
        logger.debug("Finding countries with: page: {}, size: {}", page, size);

        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        var pageable = PageRequest.of(p, s, Sort.by("name").ascending());

        return countryRepository.findAllBy(pageable)
                .map(this::toResponse);
    }

    public Mono<Long> count() {
        return countryRepository.count();
    }
    public Mono<Long> countByContinentId(long continentId) {
        return countryRepository.countAllByContinentId(continentId);
    }

    public Mono<CountryResponse> getByIsoAlpha2(String isoAlpha2) {
        logger.debug("Finding country by iso alpha 2 code: {}", isoAlpha2);

        return countryRepository.findByIsoAlpha2(isoAlpha2)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("country not found")))
                .map(this::toResponse);
    }


    public Mono<CountryResponse> getByIsoAlpha3(String isoAlpha3) {
        logger.debug("Finding country by iso alpha 3 code: {}", isoAlpha3);

        return countryRepository.findByIsoAlpha3(isoAlpha3)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("country not found")))
                .map(this::toResponse);
    }

    private CountryEntity toEntity(Country country) {
        return CountryEntity.builder()
                .id(country.id())
                .name(country.name())
                .isoAlpha2(country.isoAlpha2())
                .isoAlpha3(country.isoAlpha3())
                .capital(country.capital())
                .dialingCode(country.dialingCode())
                .flag(country.flag())
                .continentId(country.countryId())
                .build();
    }

    private Country toDomain(CountryEntity entity) {
        return Country.builder()
                .id(entity.id())
                .name(entity.name())
                .isoAlpha2(entity.isoAlpha2())
                .isoAlpha3(entity.isoAlpha3())
                .capital(entity.capital())
                .dialingCode(entity.dialingCode())
                .flag(entity.flag())
                .countryId(entity.continentId())
                .build();
    }

    private Country toDomain(CountryEntity entity, CountryUpdateRequest request) {
        return Country.builder()
                .id(entity.id())
                .name(request.name())
                .isoAlpha2(request.isoAlpha2())
                .isoAlpha3(request.isoAlpha3())
                .capital(request.capital())
                .dialingCode(request.dialingCode())
                .flag(request.flag())
                .countryId(request.continentCode())
                .build();
    }

    private CountryResponse toResponse(CountryEntity entity) {
        return new CountryResponse(
                entity.name(),
                entity.isoAlpha2(),
                entity.isoAlpha3(),
                entity.capital(),
                entity.dialingCode(),
                entity.flag(),
                entity.continentId(),
                entity.createdOn()
                 );
    }
}
