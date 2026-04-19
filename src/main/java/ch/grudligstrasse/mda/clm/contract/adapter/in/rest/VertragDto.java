package ch.grudligstrasse.mda.clm.contract.adapter.in.rest;

import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record VertragDto(
        String id,
        String titel,
        String typ,
        String stage,
        LocalDate gueltigVon,
        LocalDate gueltigBis,
        UUID erstellerId,
        long versionNumber,
        List<ParteiDto> parteien,
        List<VersionDto> versionen) {

    public static VertragDto of(Vertrag v) {
        List<ParteiDto> parteien = v.parteien().stream()
                .map(p -> new ParteiDto(p.rolle().name(), p.personId()))
                .toList();
        List<VersionDto> versionen = v.versionen().stream()
                .map(vv -> new VersionDto(
                        vv.versionNummer(),
                        vv.erstellt().toString(),
                        vv.erstelltVon(),
                        vv.dokument().mimeType(),
                        vv.dokument().groesseByte(),
                        vv.dokument().speicherTyp().name(),
                        vv.dokument().inhaltHash()))
                .toList();
        return new VertragDto(
                v.id().asString(),
                v.titel(),
                v.typ().name(),
                v.stage().name(),
                v.gueltigVon(),
                v.gueltigBis(),
                v.erstellerId(),
                v.versionNumber(),
                parteien,
                versionen);
    }

    public record ParteiDto(String rolle, UUID personId) {}

    public record VersionDto(int versionNummer, String erstellt, UUID erstelltVon,
                             String mimeType, long groesseByte, String speicherTyp,
                             String inhaltHash) {}
}
