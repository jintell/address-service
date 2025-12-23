package org.meldtech.platform.features.lgas.infra.r2dbc;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Table("local_government_areas")
public record LgaEntity(
        @Id
        Long id,
        String name,
        String slugCode,
        BigDecimal latitude,
        BigDecimal longitude,
        String stateCode,
        String countryCode,
        Instant createdOn,
        Instant updatedOn) implements Persistable<Long> {

    public LgaEntity {
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
