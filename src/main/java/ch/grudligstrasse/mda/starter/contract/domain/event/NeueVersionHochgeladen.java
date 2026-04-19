package ch.grudligstrasse.mda.starter.contract.domain.event;

import ch.grudligstrasse.mda.starter.contract.domain.DokumentVersionId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.time.Instant;
import java.util.UUID;

public record NeueVersionHochgeladen(
        UUID eventId,
        VertragId vertragId,
        DokumentVersionId versionId,
        int versionNummer,
        String pruefsummeSha256,
        Instant occurredAt,
        UUID correlationId
) implements VertragDomainEvent {

    public static NeueVersionHochgeladen now(VertragId vertragId, DokumentVersionId versionId,
                                             int versionNummer, String pruefsummeSha256, UUID correlationId) {
        return new NeueVersionHochgeladen(UUID.randomUUID(), vertragId, versionId, versionNummer,
                pruefsummeSha256, Instant.now(), correlationId);
    }

    @Override public String eventType() { return "clm.contract.neueversion.v1"; }
}
