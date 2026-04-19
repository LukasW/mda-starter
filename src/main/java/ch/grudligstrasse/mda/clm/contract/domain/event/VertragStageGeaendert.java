package ch.grudligstrasse.mda.clm.contract.domain.event;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;

import java.time.Instant;
import java.util.UUID;

public record VertragStageGeaendert(
        UUID eventId,
        Instant occurredAt,
        VertragId vertragId,
        VertragStage vonStage,
        VertragStage nachStage,
        String trigger,
        String actor) implements VertragDomainEvent {

    public static VertragStageGeaendert of(VertragId id, VertragStage from, VertragStage to, String trigger, String actor) {
        return new VertragStageGeaendert(UUID.randomUUID(), Instant.now(), id, from, to, trigger, actor);
    }

    @Override
    public String eventType() {
        return "VertragStageGeaendert";
    }
}
