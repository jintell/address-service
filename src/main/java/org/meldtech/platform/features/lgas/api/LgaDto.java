package org.meldtech.platform.features.lgas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
public final class LgaDto {

    public record LgaResponse(String name, String slugCode, BigDecimal latitude, BigDecimal longitude, String stateCode, String countryCode, Instant createdOn) { }
    public record LgaCreateRequest(
            @NotBlank(message = "Lga name cannot be empty")
            String name,
            @NotBlank
            String slugCode,
            BigDecimal latitude,
            BigDecimal longitude,
            @NotNull(message = "State code cannot be null")
            String stateCode,
            @NotBlank(message = "Country code cannot be empty")
            String countryCode) { }

    public record LgaUpdateRequest(
            @NotBlank(message = "Lga name cannot be empty")
            String name,
            @NotBlank
            String slugCode,
            BigDecimal latitude,
            BigDecimal longitude,
            @NotNull(message = "State code cannot be null")
            String stateCode,
            @NotBlank(message = "Country code cannot be empty")
            String countryCode) { }
}
