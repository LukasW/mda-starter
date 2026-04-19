package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;

import java.time.Instant;
import java.util.Objects;

public record DokumentVersion(
        DokumentVersionId id,
        VertragId vertragId,
        int versionNummer,
        String blobReferenz,
        String pruefsummeSha256,
        String dateiname,
        String mimeType,
        long groesseBytes,
        String aenderungskommentar,
        Instant hochgeladenAm,
        UserId hochgeladenVon
) {
    public DokumentVersion {
        Objects.requireNonNull(id);
        Objects.requireNonNull(vertragId);
        if (versionNummer < 1) {
            throw new DomainException("MDA-CON-040", "versionNummer muss >= 1 sein");
        }
        if (blobReferenz == null || blobReferenz.isBlank()) {
            throw new DomainException("MDA-CON-041", "blobReferenz ist Pflicht");
        }
        if (pruefsummeSha256 == null || pruefsummeSha256.isBlank()) {
            throw new DomainException("MDA-CON-042", "pruefsummeSha256 ist Pflicht");
        }
        if (groesseBytes < 0) {
            throw new DomainException("MDA-CON-043", "groesseBytes darf nicht negativ sein");
        }
        Objects.requireNonNull(hochgeladenAm);
        Objects.requireNonNull(hochgeladenVon);
    }
}
