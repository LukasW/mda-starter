package ch.grudligstrasse.mda.starter.approval.adapter.in.rest;

import ch.grudligstrasse.mda.starter.approval.domain.Entscheidung;
import ch.grudligstrasse.mda.starter.approval.domain.Freigabe;

import java.time.Instant;
import java.util.UUID;

public record FreigabeDto(
        UUID freigabeId,
        UUID vertragId,
        UUID versionId,
        UUID reviewerId,
        Entscheidung entscheidung,
        String begruendung,
        Instant angefordertAm,
        Instant entschiedenAm
) {
    public static FreigabeDto from(Freigabe f) {
        return new FreigabeDto(
                f.id().value(),
                f.vertragId(),
                f.versionId(),
                f.reviewerUserId(),
                f.entscheidung().orElse(null),
                f.begruendung().orElse(null),
                f.angefordertAm(),
                f.entschiedenAm().orElse(null));
    }
}
