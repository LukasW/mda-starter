package ch.grudligstrasse.mda.starter.obligation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FristGesichtet(
        UUID eventId,
        UUID fristId,
        UUID vertragId,
        UUID verantwortlicherId,
        Instant occurredAt,
        UUID correlationId
) implements FristDomainEvent {

    public static FristGesichtet now(UUID fristId, UUID vertragId, UUID verantwortlicherId, UUID correlationId) {
        return new FristGesichtet(UUID.randomUUID(), fristId, vertragId, verantwortlicherId,
                Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.obligation.fristgesichtet.v1"; }
}
