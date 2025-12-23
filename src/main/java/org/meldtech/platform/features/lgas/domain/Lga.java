package org.meldtech.platform.features.lgas.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Lga(Long id, String name, String slugCode, BigDecimal latitude, BigDecimal longitude, String stateCode, String countryCode) {

    public static Lga create(String name, String slugCode, BigDecimal latitude, BigDecimal longitude, String stateCode, String countryCode) {
        return new Lga(null, name, slugCode, latitude, longitude, stateCode, countryCode);
    }

    public Lga update(Lga country, String name, String slugCode, BigDecimal latitude, BigDecimal longitude, String stateCode, String countryCode) {
        return Lga.builder()
                .id(country.id())
                .name(getValue(name, country.name()))
                .slugCode(getValue(slugCode, country.slugCode()))
                .latitude(getValue(latitude, country.latitude()))
                .longitude(getValue(longitude, country.longitude()))
                .stateCode(getValue(stateCode, country.stateCode()))
                .countryCode(getValue(countryCode, country.countryCode()))
                .build();
    }

    private static String getValue(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }
    private static BigDecimal getValue(BigDecimal newValue, BigDecimal oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
