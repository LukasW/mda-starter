package ch.grudligstrasse.mda.clm.contract.domain.event;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record VertragVersionErstellt(
        UUID eventId,
        Instant occurredAt,
        VertragId vertragId,
        int versionNummer,
        String inhaltHash,
        UUID erstelltVon) implements VertragDomainEvent {

    public static VertragVersionErstellt of(VertragId id, int version, String hash, UUID erstelltVon) {
        return new VertragVersionErstellt(UUID.randomUUID(), Instant.now(), id, version, hash, erstelltVon);
    }

    @Override
    public String eventType() {
        return "VertragVersionErstellt";
    }
}
