package ch.grudligstrasse.mda.starter.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record VertragId(UUID value) {
    public VertragId {
        Objects.requireNonNull(value, "VertragId darf nicht null sein");
    }

    public static VertragId newId() {
        return new VertragId(UUID.randomUUID());
    }

    public static VertragId of(UUID value) {
        return new VertragId(value);
    }
}
