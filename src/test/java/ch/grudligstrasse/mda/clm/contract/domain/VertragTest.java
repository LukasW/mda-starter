package ch.grudligstrasse.mda.clm.contract.domain;

import ch.grudligstrasse.mda.clm.contract.domain.event.VertragDomainEvent;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragErstellt;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragPersonZugeordnet;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragStageGeaendert;
import ch.grudligstrasse.mda.clm.contract.domain.event.VertragVersionErstellt;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VertragTest {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER = UUID.randomUUID();

    @Test
    void erstellen_erzeugtEntwurfUndEvent() {
        Vertrag v = Vertrag.erstellen("Rahmenvertrag X", VertragsTyp.KOOPERATIONSVERTRAG, USER, TENANT);

        assertEquals(VertragStage.ENTWURF, v.stage());
        assertEquals("Rahmenvertrag X", v.titel());
        List<VertragDomainEvent> events = v.pullEvents();
        assertEquals(1, events.size());
        assertInstanceOf(VertragErstellt.class, events.get(0));
        assertTrue(v.pendingEvents().isEmpty());
    }

    @Test
    void erstellen_ohneTitelWirftException() {
        assertThrows(IllegalArgumentException.class,
                () -> Vertrag.erstellen("  ", VertragsTyp.KUNDENVERTRAG, USER, TENANT));
    }

    @Test
    void dokumentHochladen_erzeugtNeueVersion() {
        Vertrag v = Vertrag.erstellen("NDA", VertragsTyp.SONSTIGES, USER, TENANT);
        v.pullEvents();

        DokumentReferenz doc = DokumentReferenz.intern("/tmp/nda.pdf", "application/pdf", 1024, "abc123");
        v.dokumentHochladen(USER, doc);

        assertEquals(1, v.versionen().size());
        assertEquals(1, v.versionen().get(0).versionNummer());
        assertInstanceOf(VertragVersionErstellt.class, v.pullEvents().get(0));
    }

    @Test
    void personZuordnen_fuegtParteiUndEventHinzu() {
        Vertrag v = Vertrag.erstellen("Liefervertrag", VertragsTyp.LIEFERANTENVERTRAG, USER, TENANT);
        v.pullEvents();
        UUID personId = UUID.randomUUID();

        v.personZuordnen(ParteiRolle.AUFTRAGNEHMER, personId);

        assertEquals(1, v.parteien().size());
        assertEquals(ParteiRolle.AUFTRAGNEHMER, v.parteien().get(0).rolle());
        assertInstanceOf(VertragPersonZugeordnet.class, v.pullEvents().get(0));
    }

    @Test
    void metadatenSetzen_validiertGueltigkeitsbereich() {
        Vertrag v = Vertrag.erstellen("Test", VertragsTyp.SONSTIGES, USER, TENANT);
        assertThrows(IllegalArgumentException.class,
                () -> v.metadatenSetzen("Test", LocalDate.of(2030, 1, 1), LocalDate.of(2025, 1, 1)));
    }

    @Test
    void stageWechseln_produziertEventUndAktualisiertStage() {
        Vertrag v = Vertrag.erstellen("Vertrag", VertragsTyp.SONSTIGES, USER, TENANT);
        v.pullEvents();

        v.stageWechseln(VertragStage.IN_PRUEFUNG, "einreichen", USER.toString());

        assertEquals(VertragStage.IN_PRUEFUNG, v.stage());
        VertragStageGeaendert evt = (VertragStageGeaendert) v.pullEvents().get(0);
        assertEquals(VertragStage.ENTWURF, evt.vonStage());
        assertEquals(VertragStage.IN_PRUEFUNG, evt.nachStage());
    }

    @Test
    void archivierterVertrag_istNichtMehrEditierbar() {
        Vertrag v = Vertrag.erstellen("Vertrag", VertragsTyp.SONSTIGES, USER, TENANT);
        v.stageWechseln(VertragStage.ARCHIVIERT, "archivieren", "system");

        assertThrows(IllegalStateException.class,
                () -> v.dokumentHochladen(USER, DokumentReferenz.intern("/x", "x", 1, "h")));
    }

    @Test
    void finaleStages_sindFinal() {
        assertTrue(VertragStage.ABGELAUFEN.isFinal());
        assertTrue(VertragStage.GEKUENDIGT.isFinal());
        assertFalse(VertragStage.ENTWURF.isFinal());
        assertSame(VertragStage.ENTWURF, VertragStage.ENTWURF);
    }
}
