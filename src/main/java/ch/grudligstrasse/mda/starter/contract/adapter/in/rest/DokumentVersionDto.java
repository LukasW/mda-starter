package ch.grudligstrasse.mda.starter.contract.adapter.in.rest;

import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersion;

import java.time.Instant;
import java.util.UUID;

public record DokumentVersionDto(
        UUID versionId,
        int versionNummer,
        String dateiname,
        String mimeType,
        long groesseBytes,
        String pruefsummeSha256,
        Instant hochgeladenAm
) {
    public static DokumentVersionDto from(DokumentVersion dv) {
        return new DokumentVersionDto(
                dv.id().value(),
                dv.versionNummer(),
                dv.dateiname(),
                dv.mimeType(),
                dv.groesseBytes(),
                dv.pruefsummeSha256(),
                dv.hochgeladenAm());
    }
}
