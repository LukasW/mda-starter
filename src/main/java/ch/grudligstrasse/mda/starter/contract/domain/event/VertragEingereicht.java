package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersionId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;

import java.time.Instant;
import java.util.UUID;

public record VertragEingereicht(
        UUID eventId,
        VertragId vertragId,
        DokumentVersionId versionId,
        UserId antragstellerId,
        Vertragsart vertragsart,
        Instant occurredAt,
        UUID correlationId
) implements VertragDomainEvent {

    public static VertragEingereicht now(VertragId vertragId, DokumentVersionId versionId,
                                         UserId antragstellerId, Vertragsart vertragsart, UUID correlationId) {
        return new VertragEingereicht(UUID.randomUUID(), vertragId, versionId, antragstellerId, vertragsart,
                Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.contract.eingereicht.v1"; }
}
