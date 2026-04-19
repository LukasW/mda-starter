package ch.grudligstrasse.mda.starter.obligation.domain;

import java.util.Objects;
import java.util.UUID;

public record FristId(UUID value) {
    public FristId {
        Objects.requireNonNull(value, "FristId darf nicht null sein");
    }

    public static FristId newId() { return new FristId(UUID.randomUUID()); }
    public static FristId of(UUID value) { return new FristId(value); }
}
