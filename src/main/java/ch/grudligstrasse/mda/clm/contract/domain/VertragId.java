package ch.grudligstrasse.mda.clm.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record VertragId(UUID value) {

    public VertragId {
        Objects.requireNonNull(value, "VertragId.value must not be null");
    }

    public static VertragId generate() {
        return new VertragId(UUID.randomUUID());
    }

    public static VertragId parse(String value) {
        try {
            return new VertragId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültige VertragId: " + value, e);
        }
    }

    public String asString() {
        return value.toString();
    }
}
