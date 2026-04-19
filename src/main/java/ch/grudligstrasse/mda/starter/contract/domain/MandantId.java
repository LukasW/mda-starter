package ch.grudligstrasse.mda.starter.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record MandantId(UUID value) {
    public MandantId {
        Objects.requireNonNull(value, "MandantId darf nicht null sein");
    }

    public static MandantId of(UUID value) {
        return new MandantId(value);
    }
}
