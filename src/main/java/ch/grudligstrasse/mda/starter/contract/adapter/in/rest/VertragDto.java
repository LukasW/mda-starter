package ch.grudligstrasse.mda.starter.contract.adapter.in.rest;

import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragStatus;
import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record VertragDto(
        UUID vertragId,
        UUID mandantId,
        String titel,
        Vertragsart vertragsart,
        UUID partnerId,
        VertragStatus status,
        LocalDate startDatum,
        LocalDate endDatum,
        Integer kuendigungsfristTage,
        List<DokumentVersionDto> versionen
) {
    public static VertragDto from(Vertrag v) {
        return new VertragDto(
                v.id().value(),
                v.mandantId().value(),
                v.titel(),
                v.vertragsart(),
                v.partnerId().value(),
                v.status(),
                v.startDatum(),
                v.endDatum(),
                v.kuendigungsfristTage(),
                v.versionen().stream().map(DokumentVersionDto::from).collect(Collectors.toList()));
    }
}
