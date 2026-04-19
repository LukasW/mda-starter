package ch.grudligstrasse.mda.starter.shared.process;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BpfService {

    @Inject BpfInstanceRepository instances;
    @Inject BpfTransitionLogRepository logs;

    @Transactional
    public <S extends Enum<S> & BpfStage, T extends Enum<T>> S startOrGet(BpfDefinition<S, T> def, UUID aggregateId, String actor) {
        Optional<BpfInstanceEntity> existing = instances.findForAggregate(def.processName(), def.aggregateType(), aggregateId);
        if (existing.isPresent()) {
            return stageFromName(def, existing.get().currentStage);
        }
        BpfInstanceEntity e = new BpfInstanceEntity();
        e.id = UUID.randomUUID();
        e.processName = def.processName();
        e.aggregateType = def.aggregateType();
        e.aggregateId = aggregateId;
        e.currentStage = def.initialStage().name();
        e.startedAt = Instant.now();
        instances.persist(e);

        BpfTransitionLogEntity log = new BpfTransitionLogEntity();
        log.id = UUID.randomUUID();
        log.instanceId = e.id;
        log.fromStage = null;
        log.toStage = def.initialStage().name();
        log.triggerName = "INIT";
        log.occurredAt = Instant.now();
        log.actor = actor == null ? "system" : actor;
        logs.persist(log);
        return def.initialStage();
    }

    @Transactional
    public <S extends Enum<S> & BpfStage, T extends Enum<T>> S transition(BpfDefinition<S, T> def, UUID aggregateId, T trigger, String actor) {
        BpfInstanceEntity e = instances.findForAggregate(def.processName(), def.aggregateType(), aggregateId)
                .orElseThrow(() -> new DomainException("MDA-BPF-002", "Prozessinstanz nicht gefunden: " + def.processName() + " " + aggregateId));
        S from = stageFromName(def, e.currentStage);
        S to = def.nextStage(from, trigger);
        e.currentStage = to.name();
        if (def.isTerminal(to)) {
            e.completedAt = Instant.now();
        }
        instances.persist(e);

        BpfTransitionLogEntity log = new BpfTransitionLogEntity();
        log.id = UUID.randomUUID();
        log.instanceId = e.id;
        log.fromStage = from.name();
        log.toStage = to.name();
        log.triggerName = trigger.name();
        log.occurredAt = Instant.now();
        log.actor = actor == null ? "system" : actor;
        logs.persist(log);
        return to;
    }

    public <S extends Enum<S> & BpfStage, T extends Enum<T>> Optional<S> currentStage(BpfDefinition<S, T> def, UUID aggregateId) {
        return instances.findForAggregate(def.processName(), def.aggregateType(), aggregateId)
                .map(e -> stageFromName(def, e.currentStage));
    }

    public List<BpfTransitionLogEntity> history(String processName, String aggregateType, UUID aggregateId) {
        return instances.findForAggregate(processName, aggregateType, aggregateId)
                .map(e -> logs.findByInstance(e.id))
                .orElse(List.of());
    }

    private static <S extends Enum<S> & BpfStage, T extends Enum<T>> S stageFromName(BpfDefinition<S, T> def, String stageName) {
        for (S s : def.initialStage().getDeclaringClass().getEnumConstants()) {
            if (s.name().equals(stageName)) {
                return s;
            }
        }
        throw new DomainException("MDA-BPF-003", "Unbekannter Stage-Name: " + stageName);
    }
}
