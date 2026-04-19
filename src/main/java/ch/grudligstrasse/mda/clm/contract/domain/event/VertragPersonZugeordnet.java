package ch.grudligstrasse.mda.clm.contract.domain.event;

import ch.grudligstrasse.mda.clm.contract.domain.ParteiRolle;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record VertragPersonZugeordnet(
        UUID eventId,
        Instant occurredAt,
        VertragId vertragId,
        UUID personId,
        ParteiRolle rolle) implements VertragDomainEvent {

    public static VertragPersonZugeordnet of(VertragId id, UUID personId, ParteiRolle rolle) {
        return new VertragPersonZugeordnet(UUID.randomUUID(), Instant.now(), id, personId, rolle);
    }

    @Override
    public String eventType() {
        return "VertragPersonZugeordnet";
    }
}
