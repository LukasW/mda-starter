package ch.grudligstrasse.mda.starter.approval.domain.event;

import java.time.Instant;
import java.util.UUID;

public record FreigabeAngefordert(
        UUID eventId,
        UUID freigabeId,
        UUID vertragId,
        UUID versionId,
        UUID reviewerId,
        Instant occurredAt,
        UUID correlationId
) implements FreigabeDomainEvent {

    public static FreigabeAngefordert now(UUID freigabeId, UUID vertragId, UUID versionId,
                                          UUID reviewerId, UUID correlationId) {
        return new FreigabeAngefordert(UUID.randomUUID(), freigabeId, vertragId, versionId,
                reviewerId, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.approval.angefordert.v1"; }
}
