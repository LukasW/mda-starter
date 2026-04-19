package ch.grudligstrasse.mda.starter.approval.domain.event;

import ch.grudligstrasse.mda.starter.approval.domain.Entscheidung;

import java.time.Instant;
import java.util.UUID;

public record FreigabeEntschieden(
        UUID eventId,
        UUID freigabeId,
        UUID vertragId,
        UUID reviewerId,
        Entscheidung entscheidung,
        String begruendung,
        Instant occurredAt,
        UUID correlationId
) implements FreigabeDomainEvent {

    public static FreigabeEntschieden now(UUID freigabeId, UUID vertragId, UUID reviewerId,
                                          Entscheidung entscheidung, String begruendung, UUID correlationId) {
        return new FreigabeEntschieden(UUID.randomUUID(), freigabeId, vertragId, reviewerId, entscheidung,
                begruendung, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.approval.entschieden.v1"; }
}
