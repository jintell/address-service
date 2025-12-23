package org.meldtech.platform.features.countries.domain;

import lombok.Builder;

@Builder
public record Country(Long id, String name, String isoAlpha2, String isoAlpha3, String capital, String dialingCode, String flag, Long countryId) {

    public static Country create(String name, String isoAlpha2, String isoAlpha3, String capital, String dialingCode, String flag, Long countryId) {
        return new Country(null, name, isoAlpha2, isoAlpha3, capital, dialingCode, flag, countryId);
    }

    public Country update(Country country, String name, String isoAlpha2, String isoAlpha3, String capital, String dialingCode, String flag, Long countryId) {
        return Country.builder()
                .id(country.id())
                .name(getValue(name, country.name()))
                .isoAlpha2(getValue(isoAlpha2, country.isoAlpha2()))
                .isoAlpha3(getValue(isoAlpha3, country.isoAlpha3()))
                .capital(getValue(capital, country.capital()))
                .dialingCode(getValue(dialingCode, country.dialingCode()))
                .flag(getValue(flag, country.flag()))
                .countryId(getValue(countryId, country.countryId()))
                .build();
    }

    private static String getValue(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }
    private static Long getValue(Long newValue, Long oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
