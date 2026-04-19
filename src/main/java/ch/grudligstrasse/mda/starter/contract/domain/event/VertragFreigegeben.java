package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record VertragFreigegeben(
        UUID eventId,
        VertragId vertragId,
        UserId reviewerId,
        Instant occurredAt,
        UUID correlationId
) implements VertragDomainEvent {

    public static VertragFreigegeben now(VertragId vertragId, UserId reviewerId, UUID correlationId) {
        return new VertragFreigegeben(UUID.randomUUID(), vertragId, reviewerId, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.contract.freigegeben.v1"; }
}
