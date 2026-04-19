package ch.grudligstrasse.mda.clm.shared.process;

import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BPF-Engine. Laufzeitdispatch nach {@link BpfDefinition}. Jede Transition
 * schreibt einen Eintrag ins {@code bpf_transition_log}.
 */
@ApplicationScoped
public class BpfService {

    public static final String TENANT_DEFAULT_UUID = "00000000-0000-0000-0000-000000000001";

    private final BpfInstanceRepository instances;
    private final BpfTransitionLogRepository logs;

    public BpfService(BpfInstanceRepository instances, BpfTransitionLogRepository logs) {
        this.instances = instances;
        this.logs = logs;
    }

    @Transactional
    public <S extends Enum<S>> S start(BpfDefinition<S> def, UUID aggregateId, UUID tenantId, String actor) {
        Optional<BpfInstanceEntity> existing = instances.findActive(def.processName(), aggregateId);
        if (existing.isPresent()) {
            throw DomainException.conflict("MDA-BPF-002",
                    "Aktive Prozessinstanz bereits vorhanden für " + def.processName() + "/" + aggregateId);
        }
        BpfInstanceEntity entity = new BpfInstanceEntity();
        entity.id = UUID.randomUUID();
        entity.tenantId = tenantId;
        entity.processName = def.processName();
        entity.aggregateId = aggregateId;
        entity.aggregateType = def.aggregateType();
        entity.currentStage = def.initial().name();
        entity.startedAt = Instant.now();
        instances.persist(entity);
        writeLog(entity.id, null, def.initial().name(), "start", actor);
        return def.initial();
    }

    @Transactional
    public <S extends Enum<S>> S trigger(BpfDefinition<S> def, UUID aggregateId, String trigger, String actor) {
        BpfInstanceEntity instance = instances.findActive(def.processName(), aggregateId)
                .orElseThrow(() -> DomainException.notFound("MDA-BPF-003",
                        "Keine aktive Prozessinstanz für " + def.processName() + "/" + aggregateId));
        @SuppressWarnings("unchecked")
        Class<S> stageType = (Class<S>) def.initial().getClass();
        S from = Enum.valueOf(stageType, instance.currentStage);
        S to = def.resolve(from, trigger);
        if (to == null) {
            throw DomainException.unprocessable("MDA-BPF-001",
                    "Ungültige Transition: Trigger '" + trigger + "' aus Stage '" + from + "' nicht erlaubt.");
        }
        instance.currentStage = to.name();
        if (def.finalStages().contains(to)) {
            instance.completedAt = Instant.now();
        }
        instances.persist(instance);
        writeLog(instance.id, from.name(), to.name(), trigger, actor);
        return to;
    }

    public <S extends Enum<S>> S currentStage(BpfDefinition<S> def, UUID aggregateId) {
        return instances.findActive(def.processName(), aggregateId)
                .map(i -> {
                    @SuppressWarnings("unchecked")
                    Class<S> stageType = (Class<S>) def.initial().getClass();
                    return Enum.valueOf(stageType, i.currentStage);
                })
                .orElse(null);
    }

    public List<BpfTransitionLogEntity> history(BpfDefinition<?> def, UUID aggregateId) {
        return instances.findActive(def.processName(), aggregateId)
                .map(i -> logs.forInstance(i.id))
                .orElse(List.of());
    }

    private void writeLog(UUID instanceId, String from, String to, String trigger, String actor) {
        BpfTransitionLogEntity log = new BpfTransitionLogEntity();
        log.id = UUID.randomUUID();
        log.instanceId = instanceId;
        log.fromStage = from;
        log.toStage = to;
        log.triggerName = trigger;
        log.guardResult = "OK";
        log.occurredAt = Instant.now();
        log.actor = actor != null ? actor : "system";
        logs.persist(log);
    }
}
