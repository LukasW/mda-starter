package ch.grudligstrasse.mda.starter.obligation.domain.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FristAktiviert(
        UUID eventId,
        UUID fristId,
        UUID vertragId,
        LocalDate faelligkeitsDatum,
        Instant occurredAt,
        UUID correlationId
) implements FristDomainEvent {

    public static FristAktiviert now(UUID fristId, UUID vertragId, LocalDate faelligkeit, UUID correlationId) {
        return new FristAktiviert(UUID.randomUUID(), fristId, vertragId, faelligkeit, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.obligation.fristaktiviert.v1"; }
}
