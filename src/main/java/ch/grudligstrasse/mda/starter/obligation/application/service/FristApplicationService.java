package ch.grudligstrasse.mda.starter.obligation.application.service;

import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristErfassenUseCase;
import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristErinnernUseCase;
import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristLadenQuery;
import ch.grudligstrasse.mda.starter.obligation.application.port.in.FristSichtenUseCase;
import ch.grudligstrasse.mda.starter.obligation.application.port.out.FristRepository;
import ch.grudligstrasse.mda.starter.obligation.domain.Frist;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;
import ch.grudligstrasse.mda.starter.obligation.domain.FristTrigger;
import ch.grudligstrasse.mda.starter.obligation.domain.FristenErinnerungProcess;
import ch.grudligstrasse.mda.starter.obligation.domain.event.FristAktiviert;
import ch.grudligstrasse.mda.starter.obligation.domain.event.FristErinnerungAusgeloest;
import ch.grudligstrasse.mda.starter.obligation.domain.event.FristGesichtet;
import ch.grudligstrasse.mda.starter.shared.events.DomainEventPublisher;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import ch.grudligstrasse.mda.starter.shared.process.BpfService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FristApplicationService implements
        FristErfassenUseCase,
        FristSichtenUseCase,
        FristErinnernUseCase,
        FristLadenQuery {

    @Inject FristRepository repo;
    @Inject BpfService bpfService;
    @Inject DomainEventPublisher publisher;

    @Override
    @Transactional
    public FristId erfassen(FristErfassenUseCase.Command cmd) {
        Frist f = Frist.neu(cmd.vertragId(), cmd.art(), cmd.faelligkeitsDatum(),
                cmd.vorlaufTage(), cmd.verantwortlicherId());
        Frist saved = repo.save(f);
        bpfService.startOrGet(FristenErinnerungProcess.DEFINITION, saved.id().value(),
                cmd.verantwortlicherId().toString());
        publisher.publish(FristAktiviert.now(saved.id().value(), saved.vertragId(),
                saved.faelligkeitsDatum(), UUID.randomUUID()));
        return saved.id();
    }

    @Override
    @Transactional
    public void sichten(FristSichtenUseCase.Command cmd) {
        Frist f = repo.findById(cmd.fristId()).orElseThrow(
                () -> new DomainException("MDA-OBL-NOT_FOUND", "Frist nicht gefunden: " + cmd.fristId().value()));
        f.markiereGesichtet();
        repo.save(f);
        bpfService.transition(FristenErinnerungProcess.DEFINITION, f.id().value(),
                FristTrigger.SICHTEN, cmd.sichtenderUserId().toString());
        publisher.publish(FristGesichtet.now(f.id().value(), f.vertragId(),
                cmd.sichtenderUserId(), UUID.randomUUID()));
    }

    @Override
    @Transactional
    public int erinnere(LocalDate stichtag) {
        List<Frist> faellige = repo.findFaelligeBis(stichtag);
        int ausgeloest = 0;
        for (Frist f : faellige) {
            if (!f.istFaelligZurErinnerung(stichtag)) {
                continue;
            }
            f.markiereErinnert();
            repo.save(f);
            bpfService.transition(FristenErinnerungProcess.DEFINITION, f.id().value(),
                    FristTrigger.ERINNERUNG_AUSLOESEN, "scheduler");
            publisher.publish(FristErinnerungAusgeloest.now(
                    f.id().value(), f.vertragId(), f.verantwortlicherUserId(), UUID.randomUUID()));
            ausgeloest++;
        }
        return ausgeloest;
    }

    @Override
    public Optional<Frist> laden(FristId id) {
        return repo.findById(id);
    }

    @Override
    public List<Frist> fuerVertrag(UUID vertragId) {
        return repo.findByVertrag(vertragId);
    }

    @Override
    public List<Frist> alle() {
        return repo.findAll();
    }
}
