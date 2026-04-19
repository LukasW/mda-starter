package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record VertragErfasst(
        UUID eventId,
        VertragId vertragId,
        Instant occurredAt,
        UUID correlationId
) implements VertragDomainEvent {

    public static VertragErfasst now(VertragId vertragId, UUID correlationId) {
        return new VertragErfasst(UUID.randomUUID(), vertragId, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.contract.erfasst.v1"; }
}
