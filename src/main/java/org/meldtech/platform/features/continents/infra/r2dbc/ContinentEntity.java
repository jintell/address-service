package org.meldtech.platform.features.continents.infra.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Table("continents")
public record ContinentEntity(
        @Id
        Long id,
        String name,
        Instant createdOn,
        Instant updatedOn) implements Persistable<Long> {

    public ContinentEntity {
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
