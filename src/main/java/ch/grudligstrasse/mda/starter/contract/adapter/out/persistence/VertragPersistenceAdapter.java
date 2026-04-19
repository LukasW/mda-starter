package ch.grudligstrasse.mda.starter.contract.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.contract.application.port.out.VertragRepository;
import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersion;
import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersionId;
import ch.grudligstrasse.mda.starter.contract.domain.MandantId;
import ch.grudligstrasse.mda.starter.contract.domain.PartnerId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class VertragPersistenceAdapter implements VertragRepository {

    @Inject VertragPanacheRepository vertraege;
    @Inject DokumentVersionPanacheRepository versionen;

    @Override
    @Transactional
    public Vertrag save(Vertrag vertrag) {
        VertragJpaEntity e = vertraege.findByIdOptional(vertrag.id().value()).orElseGet(VertragJpaEntity::new);
        e.id = vertrag.id().value();
        e.mandantId = vertrag.mandantId().value();
        e.titel = vertrag.titel();
        e.vertragsart = vertrag.vertragsart();
        e.partnerId = vertrag.partnerId().value();
        e.status = vertrag.status();
        e.startDatum = vertrag.startDatum();
        e.endDatum = vertrag.endDatum();
        e.kuendigungsfristTage = vertrag.kuendigungsfristTage();
        e.vertragsverantwortlicherUserId = vertrag.vertragsverantwortlicherUserId().map(UserId::value).orElse(null);
        e.erstelltAm = vertrag.erstelltAm();
        e.erstelltVon = vertrag.erstelltVon().value();
        vertraege.getEntityManager().merge(e);

        Set<UUID> knownIds = new HashSet<>();
        for (DokumentVersion dv : vertrag.versionen()) {
            knownIds.add(dv.id().value());
            DokumentVersionJpaEntity de = versionen.findByIdOptional(dv.id().value()).orElseGet(DokumentVersionJpaEntity::new);
            de.id = dv.id().value();
            de.vertragId = dv.vertragId().value();
            de.versionNummer = dv.versionNummer();
            de.blobReferenz = dv.blobReferenz();
            de.pruefsummeSha256 = dv.pruefsummeSha256();
            de.dateiname = dv.dateiname();
            de.mimeType = dv.mimeType();
            de.groesseBytes = dv.groesseBytes();
            de.aenderungskommentar = dv.aenderungskommentar();
            de.hochgeladenAm = dv.hochgeladenAm();
            de.hochgeladenVon = dv.hochgeladenVon().value();
            versionen.getEntityManager().merge(de);
        }
        return vertrag;
    }

    @Override
    public Optional<Vertrag> findById(VertragId id) {
        return vertraege.findByIdOptional(id.value()).map(this::toDomain);
    }

    @Override
    public List<Vertrag> findAll() {
        return vertraege.listAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Vertrag toDomain(VertragJpaEntity e) {
        List<DokumentVersion> dvList = versionen.findByVertrag(e.id).stream().map(this::toDomain).toList();
        return Vertrag.rehydrate(
                VertragId.of(e.id),
                MandantId.of(e.mandantId),
                e.titel,
                e.vertragsart,
                PartnerId.of(e.partnerId),
                e.status,
                e.startDatum,
                e.endDatum,
                e.kuendigungsfristTage,
                e.vertragsverantwortlicherUserId == null ? null : UserId.of(e.vertragsverantwortlicherUserId),
                e.erstelltAm,
                UserId.of(e.erstelltVon),
                new ArrayList<>(dvList));
    }

    private DokumentVersion toDomain(DokumentVersionJpaEntity de) {
        return new DokumentVersion(
                DokumentVersionId.of(de.id),
                VertragId.of(de.vertragId),
                de.versionNummer,
                de.blobReferenz,
                de.pruefsummeSha256,
                de.dateiname,
                de.mimeType,
                de.groesseBytes,
                de.aenderungskommentar,
                de.hochgeladenAm,
                UserId.of(de.hochgeladenVon));
    }
}
