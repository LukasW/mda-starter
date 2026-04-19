package ch.grudligstrasse.mda.starter.contract.domain;

import java.util.Objects;
import java.util.UUID;

public record DokumentVersionId(UUID value) {
    public DokumentVersionId {
        Objects.requireNonNull(value, "DokumentVersionId darf nicht null sein");
    }

    public static DokumentVersionId newId() {
        return new DokumentVersionId(UUID.randomUUID());
    }

    public static DokumentVersionId of(UUID value) {
        return new DokumentVersionId(value);
    }
}
