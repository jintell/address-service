package org.meldtech.platform.features.countries.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.Instant;

@NoArgsConstructor
public final class CountryDto {

    public record CountryResponse(String name, String isoAlpha2, String isoAlpha3, String capital, String dialingCode, String flag, Long continentCode, Instant createdOn) { }
    public record CountryCreateRequest(
            @NotBlank(message = "Country name cannot be empty")
            String name,
            @NotBlank
            @Range(min = 2, max = 2, message = "ISO Alpha 2 code must be 2 characters long")
            String isoAlpha2,
            @NotBlank
            @Range(min = 3, max = 3, message = "ISO Alpha 3 code must be 3 characters long")
            String isoAlpha3,
            @NotBlank(message = "Capital cannot be empty")
            String capital,
            String dialingCode,
            String flag,
            @NotNull(message = "Continent code cannot be null")
            @Positive(message = "Continent code must be positive")
            Long continentCode) { }

    public record CountryUpdateRequest(
            String name,
            String isoAlpha2,
            String isoAlpha3,
            String capital,
            String dialingCode,
            String flag,
            Long continentCode) { }
}
