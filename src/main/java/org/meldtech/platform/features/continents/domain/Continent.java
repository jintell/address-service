package org.meldtech.platform.features.continents.domain;

import lombok.Builder;

@Builder
public record Continent(Long id, String name) {

    public static  Continent create(String name) {
        return new Continent(null, name);
    }

    public Continent update(Continent continent, String name) {
        return Continent.builder()
                .id(continent.id())
                .name(getValue(name, continent.name()))
                .build();
    }

    private static String getValue(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
