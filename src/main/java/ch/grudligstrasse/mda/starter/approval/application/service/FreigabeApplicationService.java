package ch.grudligstrasse.mda.starter.approval.application.service;

import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeAnfordernUseCase;
import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeEntscheidenUseCase;
import ch.grudligstrasse.mda.starter.approval.application.port.in.FreigabeLadenQuery;
import ch.grudligstrasse.mda.starter.approval.application.port.out.FreigabeRepository;
import ch.grudligstrasse.mda.starter.approval.domain.Freigabe;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;
import ch.grudligstrasse.mda.starter.approval.domain.event.FreigabeAngefordert;
import ch.grudligstrasse.mda.starter.approval.domain.event.FreigabeEntschieden;
import ch.grudligstrasse.mda.starter.shared.events.DomainEventPublisher;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FreigabeApplicationService implements
        FreigabeAnfordernUseCase,
        FreigabeEntscheidenUseCase,
        FreigabeLadenQuery {

    @Inject FreigabeRepository repo;
    @Inject DomainEventPublisher publisher;

    @Override
    @Transactional
    public FreigabeId anfordern(FreigabeAnfordernUseCase.Command cmd) {
        Freigabe f = Freigabe.anfordern(cmd.vertragId(), cmd.versionId(), cmd.reviewerId());
        Freigabe saved = repo.save(f);
        publisher.publish(FreigabeAngefordert.now(
                saved.id().value(), saved.vertragId(), saved.versionId(), saved.reviewerUserId(), UUID.randomUUID()));
        return saved.id();
    }

    @Override
    @Transactional
    public void entscheiden(FreigabeEntscheidenUseCase.Command cmd) {
        Freigabe f = repo.findById(cmd.freigabeId()).orElseThrow(
                () -> new DomainException("MDA-APV-NOT_FOUND", "Freigabe nicht gefunden: " + cmd.freigabeId().value()));
        f.entscheiden(cmd.entscheidung(), cmd.begruendung());
        repo.save(f);
        publisher.publish(FreigabeEntschieden.now(
                f.id().value(), f.vertragId(), f.reviewerUserId(),
                cmd.entscheidung(), cmd.begruendung(), UUID.randomUUID()));
    }

    @Override
    public Optional<Freigabe> laden(FreigabeId id) {
        return repo.findById(id);
    }

    @Override
    public List<Freigabe> fuerVertrag(UUID vertragId) {
        return repo.findByVertrag(vertragId);
    }
}
