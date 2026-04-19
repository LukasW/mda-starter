package ch.grudligstrasse.mda.clm.contract.domain;

import ch.grudligstrasse.mda.clm.contract.domain.process.VertragLifecycle;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class VertragLifecycleTest {

    private final VertragLifecycle lc = VertragLifecycle.instance();

    @Test
    void happyPath_erstellungBisArchiv() {
        assertEquals(VertragStage.IN_PRUEFUNG,  lc.resolve(VertragStage.ENTWURF,         "einreichen"));
        assertEquals(VertragStage.FREIGEGEBEN,  lc.resolve(VertragStage.IN_PRUEFUNG,     "freigeben"));
        assertEquals(VertragStage.ZUR_SIGNATUR, lc.resolve(VertragStage.FREIGEGEBEN,     "zurSignaturSenden"));
        assertEquals(VertragStage.UNTERZEICHNET,lc.resolve(VertragStage.ZUR_SIGNATUR,    "unterzeichnen"));
        assertEquals(VertragStage.ARCHIVIERT,   lc.resolve(VertragStage.UNTERZEICHNET,   "archivieren"));
        assertEquals(VertragStage.ABGELAUFEN,   lc.resolve(VertragStage.ARCHIVIERT,      "ablaufen"));
        assertEquals(VertragStage.GEKUENDIGT,   lc.resolve(VertragStage.ARCHIVIERT,      "kuendigen"));
    }

    @Test
    void korrekturbedarf_undRueckkehr() {
        assertEquals(VertragStage.KORREKTURBEDARF, lc.resolve(VertragStage.IN_PRUEFUNG, "korrekturbeantragen"));
        assertEquals(VertragStage.IN_PRUEFUNG,     lc.resolve(VertragStage.KORREKTURBEDARF, "einreichen"));
    }

    @Test
    void ungueltigeTransition_liefertNull() {
        assertNull(lc.resolve(VertragStage.ENTWURF, "unterzeichnen"));
        assertNull(lc.resolve(VertragStage.ARCHIVIERT, "einreichen"));
    }

    @Test
    void finalStages_habenKeineAusgaenge() {
        Map<VertragStage, List<VertragLifecycle.Transition<VertragStage>>> tx = lc.transitions();
        assertNotNull(tx);
        // Finale Stages sind abwesend aus der Transitions-Map.
        for (VertragStage f : lc.finalStages()) {
            assertNull(tx.get(f), "Finale Stage " + f + " darf keine Transitionen haben.");
        }
    }
}
