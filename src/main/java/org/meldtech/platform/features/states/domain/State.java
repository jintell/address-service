package org.meldtech.platform.features.states.domain;

import lombok.Builder;

import java.time.Instant;

@Builder
public record State(Long id, String name, String code, String postalCode, String countryCode) {

    public static State create(String name, String code, String postalCode, String countryCode) {
        return new State(null, name, code, postalCode, countryCode);
    }

    public State update(State state, String name, String code, String postalCode, String countryCode) {
        return State.builder()
                .id(state.id())
                .name(getValue(name, state.name()))
                .code(getValue(code, state.code()))
                .postalCode(getValue(postalCode, state.postalCode()))
                .countryCode(getValue(countryCode, state.countryCode()))
                .build();
    }

    private static String getValue(String newValue, String oldValue) {
        return newValue != null ? newValue : oldValue;
    }
}
