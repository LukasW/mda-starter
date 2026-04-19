package ch.grudligstrasse.mda.clm.contract.domain.event;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsTyp;

import java.time.Instant;
import java.util.UUID;

public record VertragErstellt(
        UUID eventId,
        Instant occurredAt,
        VertragId vertragId,
        String titel,
        VertragsTyp typ,
        UUID erstellerId,
        UUID tenantId) implements VertragDomainEvent {

    public static VertragErstellt of(VertragId id, String titel, VertragsTyp typ, UUID erstellerId, UUID tenantId) {
        return new VertragErstellt(UUID.randomUUID(), Instant.now(), id, titel, typ, erstellerId, tenantId);
    }

    @Override
    public String eventType() {
        return "VertragErstellt";
    }
}
