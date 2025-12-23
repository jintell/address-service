package org.meldtech.platform.features.countries.infra.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Table("countries")
public record CountryEntity(
        @Id
        Long id,
        String name,
        String isoAlpha2,
        String isoAlpha3,
        String capital,
        String dialingCode,
        String flag,
        Long continentId,
        Instant createdOn,
        Instant updatedOn) implements Persistable<Long> {

    public CountryEntity {
        if (id == null) {
            createdOn = updatedOn = Instant.now();
        }else {
            updatedOn = Instant.now();
        }
    }
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
