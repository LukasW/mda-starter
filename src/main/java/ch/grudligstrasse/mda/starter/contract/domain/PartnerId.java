package ch.grudligstrasse.mda.starter.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record PartnerId(UUID value) {
    public PartnerId {
        Objects.requireNonNull(value, "PartnerId darf nicht null sein");
    }

    public static PartnerId of(UUID value) {
        return new PartnerId(value);
    }
}
