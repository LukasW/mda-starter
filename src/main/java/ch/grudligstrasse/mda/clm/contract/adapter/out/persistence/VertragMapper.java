package ch.grudligstrasse.mda.clm.contract.adapter.out.persistence;

import ch.grudligstrasse.mda.clm.contract.domain.DokumentReferenz;
import ch.grudligstrasse.mda.clm.contract.domain.ParteiRolle;
import ch.grudligstrasse.mda.clm.contract.domain.SpeicherTyp;
import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsPartei;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsTyp;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsVersion;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;

import java.util.List;
import java.util.UUID;

final class VertragMapper {

    private VertragMapper() {}

    static Vertrag toDomain(VertragJpaEntity e) {
        List<VertragsPartei> parteien = e.parteien.stream()
                .map(p -> new VertragsPartei(ParteiRolle.valueOf(p.rolle), p.personId))
                .toList();
        List<VertragsVersion> versionen = e.versionen.stream()
                .map(v -> new VertragsVersion(v.versionNummer, v.erstellt, v.erstelltVon,
                        new DokumentReferenz(SpeicherTyp.valueOf(v.speicherTyp),
                                v.pfadLokal, v.archivExternId, v.mimeType,
                                v.groesseByte, v.inhaltHash)))
                .toList();
        return Vertrag.rehydrate(
                new VertragId(e.id), e.tenantId, e.titel,
                VertragsTyp.valueOf(e.typ),
                e.gueltigVon, e.gueltigBis,
                VertragStage.valueOf(e.stage),
                e.erstellerId, e.erstelltAm, e.modifiedAt, e.versionNumber,
                parteien, versionen);
    }

    static VertragJpaEntity toEntity(Vertrag v, VertragJpaEntity existing) {
        VertragJpaEntity e = existing != null ? existing : new VertragJpaEntity();
        e.id = v.id().value();
        e.tenantId = v.tenantId();
        e.titel = v.titel();
        e.typ = v.typ().name();
        e.stage = v.stage().name();
        e.gueltigVon = v.gueltigVon();
        e.gueltigBis = v.gueltigBis();
        e.erstellerId = v.erstellerId();
        e.erstelltAm = v.erstelltAm();
        e.modifiedAt = v.modifiedAt();
        e.parteien.clear();
        for (VertragsPartei p : v.parteien()) {
            VertragsParteiJpaEntity pe = new VertragsParteiJpaEntity();
            pe.id = UUID.randomUUID();
            pe.rolle = p.rolle().name();
            pe.personId = p.personId();
            e.parteien.add(pe);
        }
        e.versionen.clear();
        for (VertragsVersion vv : v.versionen()) {
            VertragsVersionJpaEntity ve = new VertragsVersionJpaEntity();
            ve.id = UUID.randomUUID();
            ve.versionNummer = vv.versionNummer();
            ve.erstellt = vv.erstellt();
            ve.erstelltVon = vv.erstelltVon();
            ve.speicherTyp = vv.dokument().speicherTyp().name();
            ve.pfadLokal = vv.dokument().pfadLokal();
            ve.archivExternId = vv.dokument().archivExternId();
            ve.mimeType = vv.dokument().mimeType();
            ve.groesseByte = vv.dokument().groesseByte();
            ve.inhaltHash = vv.dokument().inhaltHash();
            e.versionen.add(ve);
        }
        return e;
    }
}
