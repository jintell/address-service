package org.meldtech.platform.features.states.infra.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Table("states")
public record StateEntity(
        @Id
        Long id,
        String name,
        String code,
        String postalCode,
        String countryCode,
        Instant createdOn,
        Instant updatedOn) implements Persistable<Long> {

    public StateEntity {
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
