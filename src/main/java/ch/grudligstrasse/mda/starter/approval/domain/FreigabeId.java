package ch.grudligstrasse.mda.starter.approval.domain;

import java.util.Objects;
import java.util.UUID;

public record FreigabeId(UUID value) {
    public FreigabeId {
        Objects.requireNonNull(value, "FreigabeId darf nicht null sein");
    }

    public static FreigabeId newId() { return new FreigabeId(UUID.randomUUID()); }
    public static FreigabeId of(UUID value) { return new FreigabeId(value); }
}
