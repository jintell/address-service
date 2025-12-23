package org.meldtech.platform.features.continents.api;

import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
public final class ContinentDto {

    public record ContinentResponse(String name, Instant createdOn) { }
    public record ContinentRequest(String name) { }
}
