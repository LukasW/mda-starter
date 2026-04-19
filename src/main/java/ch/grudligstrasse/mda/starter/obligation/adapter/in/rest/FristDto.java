package ch.grudligstrasse.mda.starter.obligation.adapter.in.rest;

import ch.grudligstrasse.mda.starter.obligation.domain.Frist;
import ch.grudligstrasse.mda.starter.obligation.domain.FristArt;
import ch.grudligstrasse.mda.starter.obligation.domain.FristStatus;

import java.time.LocalDate;
import java.util.UUID;

public record FristDto(
        UUID fristId,
        UUID vertragId,
        FristArt art,
        LocalDate faelligkeitsDatum,
        int vorlaufTage,
        LocalDate erinnerungsDatum,
        FristStatus status,
        UUID verantwortlicherUserId
) {
    public static FristDto from(Frist f) {
        return new FristDto(
                f.id().value(),
                f.vertragId(),
                f.art(),
                f.faelligkeitsDatum(),
                f.vorlaufTage(),
                f.erinnerungsDatum(),
                f.status(),
                f.verantwortlicherUserId());
    }
}
