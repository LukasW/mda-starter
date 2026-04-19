package ch.grudligstrasse.mda.starter.obligation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FristErinnerungAusgeloest(
        UUID eventId,
        UUID fristId,
        UUID vertragId,
        UUID empfaengerId,
        Instant occurredAt,
        UUID correlationId
) implements FristDomainEvent {

    public static FristErinnerungAusgeloest now(UUID fristId, UUID vertragId, UUID empfaengerId, UUID correlationId) {
        return new FristErinnerungAusgeloest(UUID.randomUUID(), fristId, vertragId, empfaengerId,
                Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.obligation.erinnerungausgeloest.v1"; }
}
