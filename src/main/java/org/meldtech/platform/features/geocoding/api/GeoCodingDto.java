package org.meldtech.platform.features.geocoding.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class GeoCodingDto {
    public record GeoCodingResponse(@JsonProperty("display_name")
                                    String displayName,
                                    @JsonProperty("lat")
                                    String latitude,
                                    @JsonProperty("lon")
                                    String longitude,
                                    @JsonProperty("addresstype")
                                    String addressType) { }
}
