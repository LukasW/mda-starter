package ch.grudligstrasse.mda.starter.contract.application.service;

import ch.grudligstrasse.mda.starter.contract.application.port.in.NeueVersionHochladenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragEinreichenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragEntscheidenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragErfassenUseCase;
import ch.grudligstrasse.mda.starter.contract.application.port.in.VertragLadenQuery;
import ch.grudligstrasse.mda.starter.contract.application.port.out.VertragRepository;
import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersion;
import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersionId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragLifecycle;
import ch.grudligstrasse.mda.starter.contract.domain.VertragStatus;
import ch.grudligstrasse.mda.starter.contract.domain.VertragTrigger;
import ch.grudligstrasse.mda.starter.contract.domain.event.NeueVersionHochgeladen;
import ch.grudligstrasse.mda.starter.contract.domain.event.VertragAbgelehnt;
import ch.grudligstrasse.mda.starter.contract.domain.event.VertragEingereicht;
import ch.grudligstrasse.mda.starter.contract.domain.event.VertragErfasst;
import ch.grudligstrasse.mda.starter.contract.domain.event.VertragFreigegeben;
import ch.grudligstrasse.mda.starter.shared.events.DomainEventPublisher;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import ch.grudligstrasse.mda.starter.shared.process.BpfService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VertragApplicationService implements
        VertragErfassenUseCase,
        VertragEinreichenUseCase,
        VertragEntscheidenUseCase,
        NeueVersionHochladenUseCase,
        VertragLadenQuery {

    @Inject VertragRepository repo;
    @Inject BpfService bpfService;
    @Inject DomainEventPublisher publisher;

    @Override
    @Transactional
    public VertragId erfassen(VertragErfassenUseCase.Command cmd) {
        Vertrag v = Vertrag.erfassen(
                cmd.mandantId(),
                cmd.titel(),
                cmd.vertragsart(),
                cmd.partnerId(),
                cmd.startDatum(),
                cmd.endDatum(),
                cmd.kuendigungsfristTage(),
                cmd.antragstellerId());
        Vertrag saved = repo.save(v);
        bpfService.startOrGet(VertragLifecycle.DEFINITION, saved.id().value(), actor(cmd.antragstellerId().value()));
        publisher.publish(VertragErfasst.now(saved.id(), UUID.randomUUID()));
        return saved.id();
    }

    @Override
    @Transactional
    public void einreichen(VertragEinreichenUseCase.Command cmd) {
        Vertrag v = mustFind(cmd.vertragId());
        v.markiereEingereicht();
        repo.save(v);
        bpfService.transition(VertragLifecycle.DEFINITION, v.id().value(),
                VertragTrigger.EINREICHEN, actor(cmd.antragstellerId().value()));
        DokumentVersion aktiv = v.aktiveVersion().orElseThrow(
                () -> new DomainException("MDA-CON-050", "Aktive Version fehlt"));
        publisher.publish(VertragEingereicht.now(
                v.id(), aktiv.id(), cmd.antragstellerId(), v.vertragsart(), UUID.randomUUID()));
    }

    @Override
    @Transactional
    public void genehmigen(GenehmigenCommand cmd) {
        Vertrag v = mustFind(cmd.vertragId());
        v.markiereFreigegeben();
        repo.save(v);
        bpfService.transition(VertragLifecycle.DEFINITION, v.id().value(),
                VertragTrigger.GENEHMIGEN, actor(cmd.reviewerId().value()));
        publisher.publish(VertragFreigegeben.now(v.id(), cmd.reviewerId(), UUID.randomUUID()));
    }

    @Override
    @Transactional
    public void ablehnen(AblehnenCommand cmd) {
        if (cmd.begruendung() == null || cmd.begruendung().isBlank()) {
            throw new DomainException("MDA-CON-060", "Begruendung ist bei Ablehnung Pflicht");
        }
        Vertrag v = mustFind(cmd.vertragId());
        v.markiereAbgelehnt();
        repo.save(v);
        bpfService.transition(VertragLifecycle.DEFINITION, v.id().value(),
                VertragTrigger.ABLEHNEN, actor(cmd.reviewerId().value()));
        publisher.publish(VertragAbgelehnt.now(v.id(), cmd.reviewerId(), cmd.begruendung(), UUID.randomUUID()));
    }

    @Override
    @Transactional
    public DokumentVersionId hochladen(NeueVersionHochladenUseCase.Command cmd) {
        Vertrag v = mustFind(cmd.vertragId());
        VertragStatus vorher = v.status();
        DokumentVersion dv = v.neueVersionHochladen(
                cmd.blobReferenz(), cmd.pruefsummeSha256(), cmd.dateiname(),
                cmd.mimeType(), cmd.groesseBytes(), cmd.aenderungskommentar(), cmd.hochladender());
        repo.save(v);
        if (vorher == VertragStatus.UEBERARBEITUNG) {
            bpfService.transition(VertragLifecycle.DEFINITION, v.id().value(),
                    VertragTrigger.NEUE_VERSION_HOCHLADEN, actor(cmd.hochladender().value()));
        }
        publisher.publish(NeueVersionHochgeladen.now(
                v.id(), dv.id(), dv.versionNummer(), dv.pruefsummeSha256(), UUID.randomUUID()));
        return dv.id();
    }

    @Override
    public Optional<Vertrag> laden(VertragId id) {
        return repo.findById(id);
    }

    @Override
    public List<Vertrag> alle() {
        return repo.findAll();
    }

    private Vertrag mustFind(VertragId id) {
        return repo.findById(id).orElseThrow(
                () -> new DomainException("MDA-CON-NOT_FOUND", "Vertrag nicht gefunden: " + id.value()));
    }

    private static String actor(UUID userId) {
        return userId == null ? "system" : userId.toString();
    }
}
