package ch.grudligstrasse.mda.clm.contract.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable snapshot je Vertragsinhalt. Eine neue Version entsteht bei jeder Dokumentaenderung.
 */
public record VertragsVersion(
        int versionNummer,
        Instant erstellt,
        UUID erstelltVon,
        DokumentReferenz dokument) {

    public VertragsVersion {
        if (versionNummer < 1) {
            throw new IllegalArgumentException("versionNummer muss >= 1 sein.");
        }
        Objects.requireNonNull(erstellt, "erstellt darf nicht null sein.");
        Objects.requireNonNull(erstelltVon, "erstelltVon darf nicht null sein.");
        Objects.requireNonNull(dokument, "dokument darf nicht null sein.");
    }
}
