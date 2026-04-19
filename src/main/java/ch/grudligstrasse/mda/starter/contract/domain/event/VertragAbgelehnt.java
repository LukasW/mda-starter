package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record VertragAbgelehnt(
        UUID eventId,
        VertragId vertragId,
        UserId reviewerId,
        String begruendung,
        Instant occurredAt,
        UUID correlationId
) implements VertragDomainEvent {

    public static VertragAbgelehnt now(VertragId vertragId, UserId reviewerId, String begruendung, UUID correlationId) {
        return new VertragAbgelehnt(UUID.randomUUID(), vertragId, reviewerId, begruendung, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.contract.abgelehnt.v1"; }
}
