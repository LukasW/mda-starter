package ch.grudligstrasse.mda.clm.contract.application.service;

import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragAbrufenQuery;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragDokumentHochladenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragErstellenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragMetadatenSetzenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragPersonZuordnenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragTriggerUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.out.VertragRepository;
import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragLifecycle;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import ch.grudligstrasse.mda.clm.shared.events.DomainEventPublisher;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import ch.grudligstrasse.mda.clm.shared.process.BpfService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VertragApplicationService implements
        VertragErstellenUseCase,
        VertragMetadatenSetzenUseCase,
        VertragDokumentHochladenUseCase,
        VertragPersonZuordnenUseCase,
        VertragTriggerUseCase,
        VertragAbrufenQuery {

    private final VertragRepository repository;
    private final DomainEventPublisher publisher;
    private final BpfService bpfService;

    public VertragApplicationService(VertragRepository repository,
                                     DomainEventPublisher publisher,
                                     BpfService bpfService) {
        this.repository = repository;
        this.publisher = publisher;
        this.bpfService = bpfService;
    }

    @Override
    @Transactional
    public VertragId execute(VertragErstellenCommand cmd) {
        UUID tenant = cmd.tenantId() != null ? cmd.tenantId()
                : UUID.fromString(BpfService.TENANT_DEFAULT_UUID);
        Vertrag v = Vertrag.erstellen(cmd.titel(), cmd.typ(), cmd.erstellerId(), tenant);
        repository.save(v);
        bpfService.start(VertragLifecycle.instance(), v.id().value(), tenant, actor(cmd.erstellerId()));
        publisher.publish(v.pullEvents());
        return v.id();
    }

    @Override
    @Transactional
    public void execute(VertragMetadatenSetzenCommand cmd) {
        Vertrag v = load(cmd.vertragId());
        v.metadatenSetzen(cmd.titel(), cmd.gueltigVon(), cmd.gueltigBis());
        repository.save(v);
        publisher.publish(v.pullEvents());
    }

    @Override
    @Transactional
    public void execute(VertragDokumentHochladenCommand cmd) {
        Vertrag v = load(cmd.vertragId());
        v.dokumentHochladen(cmd.erstelltVon(), cmd.dokument());
        repository.save(v);
        publisher.publish(v.pullEvents());
    }

    @Override
    @Transactional
    public void execute(VertragPersonZuordnenCommand cmd) {
        Vertrag v = load(cmd.vertragId());
        v.personZuordnen(cmd.rolle(), cmd.personId());
        repository.save(v);
        publisher.publish(v.pullEvents());
    }

    @Override
    @Transactional
    public VertragStage execute(VertragTriggerCommand cmd) {
        Vertrag v = load(cmd.vertragId());
        VertragStage neu = bpfService.trigger(VertragLifecycle.instance(), v.id().value(),
                cmd.trigger(), cmd.actor());
        v.stageWechseln(neu, cmd.trigger(), cmd.actor());
        repository.save(v);
        publisher.publish(v.pullEvents());
        return neu;
    }

    @Override
    public Optional<Vertrag> byId(VertragId id) {
        return repository.findById(id);
    }

    @Override
    public List<Vertrag> byTenant(UUID tenantId, int top, int skip) {
        UUID tenant = tenantId != null ? tenantId : UUID.fromString(BpfService.TENANT_DEFAULT_UUID);
        return repository.findByTenant(tenant, top, skip);
    }

    private Vertrag load(VertragId id) {
        return repository.findById(id)
                .orElseThrow(() -> DomainException.notFound("MDA-CON-404",
                        "Vertrag nicht gefunden: " + id.asString()));
    }

    private static String actor(UUID erstellerId) {
        return erstellerId != null ? erstellerId.toString() : "system";
    }
}
