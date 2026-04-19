package ch.grudligstrasse.mda.starter.approval.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FreigabeTest {

    @Test
    void genehmigte_freigabe_schreibt_entschiedenAm() {
        Freigabe f = Freigabe.anfordern(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        f.entscheiden(Entscheidung.GENEHMIGT, null);
        assertEquals(Entscheidung.GENEHMIGT, f.entscheidung().orElseThrow());
        assertTrue(f.entschiedenAm().isPresent());
    }

    @Test
    void ablehnung_ohne_begruendung_wird_abgelehnt() {
        Freigabe f = Freigabe.anfordern(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        DomainException ex = assertThrows(DomainException.class, () -> f.entscheiden(Entscheidung.ABGELEHNT, " "));
        assertEquals("MDA-APV-002", ex.code());
    }

    @Test
    void doppelte_entscheidung_wird_abgelehnt() {
        Freigabe f = Freigabe.anfordern(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        f.entscheiden(Entscheidung.GENEHMIGT, null);
        DomainException ex = assertThrows(DomainException.class, () -> f.entscheiden(Entscheidung.ABGELEHNT, "Nein"));
        assertEquals("MDA-APV-001", ex.code());
    }
}
