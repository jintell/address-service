package org.meldtech.platform.features.states.api;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public final class StateDto {

    public record StateResponse(String name, String code, String postalCode, String countryCode, Instant createdOn) { }
    public record StateCreateRequest(
            @NotBlank(message = "State name cannot be empty")
            String name,
            @NotBlank(message = "State code cannot be empty")
            String code,
            String postalCode,
            @NotBlank(message = "Country code cannot be empty")
            String countryCode) { }

    public record StateUpdateRequest(
            String name,
            String code,
            String postalCode,
            String countryCode) { }
}
