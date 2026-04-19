package ch.grudligstrasse.mda.starter.contract.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DokumentVersionTest {

    @Test
    void versionNummer_muss_mindestens_eins_sein() {
        DomainException ex = assertThrows(DomainException.class, () -> new DokumentVersion(
                DokumentVersionId.newId(),
                VertragId.newId(),
                0,
                "blob://a",
                "sha",
                "v.pdf",
                "application/pdf",
                1L,
                "k",
                Instant.now(),
                UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-040", ex.code());
    }

    @Test
    void leere_blob_referenz_wird_abgelehnt() {
        DomainException ex = assertThrows(DomainException.class, () -> new DokumentVersion(
                DokumentVersionId.newId(), VertragId.newId(), 1, " ", "sha",
                "v.pdf", "application/pdf", 1L, "k", Instant.now(),
                UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-041", ex.code());
    }

    @Test
    void negative_groesse_wird_abgelehnt() {
        DomainException ex = assertThrows(DomainException.class, () -> new DokumentVersion(
                DokumentVersionId.newId(), VertragId.newId(), 1, "blob://a", "sha",
                "v.pdf", "application/pdf", -1L, "k", Instant.now(),
                UserId.of(UUID.randomUUID())));
        assertEquals("MDA-CON-043", ex.code());
    }
}
