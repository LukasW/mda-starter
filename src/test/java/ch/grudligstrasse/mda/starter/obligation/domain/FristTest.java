package ch.grudligstrasse.mda.starter.obligation.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FristTest {

    @Test
    void erinnerungsDatum_wird_aus_faelligkeit_minus_vorlauf_berechnet() {
        Frist f = Frist.neu(UUID.randomUUID(), FristArt.KUENDIGUNG,
                LocalDate.of(2026, 6, 1), 30, UUID.randomUUID());
        assertEquals(LocalDate.of(2026, 5, 2), f.erinnerungsDatum());
        assertEquals(FristStatus.OFFEN, f.status());
    }

    @Test
    void negativer_vorlauf_wird_abgelehnt() {
        DomainException ex = assertThrows(DomainException.class, () ->
                Frist.neu(UUID.randomUUID(), FristArt.ABLAUF, LocalDate.of(2026, 1, 1), -1, UUID.randomUUID()));
        assertEquals("MDA-OBL-001", ex.code());
    }

    @Test
    void offene_frist_ist_bei_erreichtem_erinnerungsdatum_faellig() {
        Frist f = Frist.neu(UUID.randomUUID(), FristArt.KUENDIGUNG,
                LocalDate.of(2026, 6, 1), 30, UUID.randomUUID());
        assertTrue(f.istFaelligZurErinnerung(LocalDate.of(2026, 5, 2)));
        assertFalse(f.istFaelligZurErinnerung(LocalDate.of(2026, 5, 1)));
    }

    @Test
    void sichten_aus_offen_wird_abgelehnt() {
        Frist f = Frist.neu(UUID.randomUUID(), FristArt.KUENDIGUNG,
                LocalDate.of(2026, 6, 1), 30, UUID.randomUUID());
        DomainException ex = assertThrows(DomainException.class, f::markiereGesichtet);
        assertEquals("MDA-OBL-011", ex.code());
    }

    @Test
    void eskalieren_aus_erinnert_funktioniert() {
        Frist f = Frist.neu(UUID.randomUUID(), FristArt.KUENDIGUNG,
                LocalDate.of(2026, 6, 1), 30, UUID.randomUUID());
        f.markiereErinnert();
        f.markiereEskaliert();
        assertEquals(FristStatus.ESKALIERT, f.status());
    }
}
