package ch.grudligstrasse.mda.starter.obligation.domain;

import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FristenErinnerungProcessTest {

    @Test
    void vollstaendiger_goldener_pfad() {
        var def = FristenErinnerungProcess.DEFINITION;
        assertEquals(FristStatus.ERINNERT, def.nextStage(FristStatus.OFFEN, FristTrigger.ERINNERUNG_AUSLOESEN));
        assertEquals(FristStatus.ERLEDIGT, def.nextStage(FristStatus.ERINNERT, FristTrigger.SICHTEN));
        assertTrue(def.isTerminal(FristStatus.ERLEDIGT));
    }

    @Test
    void eskalation_und_sichten() {
        var def = FristenErinnerungProcess.DEFINITION;
        assertEquals(FristStatus.ESKALIERT, def.nextStage(FristStatus.ERINNERT, FristTrigger.ESKALIEREN));
        assertEquals(FristStatus.ERLEDIGT, def.nextStage(FristStatus.ESKALIERT, FristTrigger.SICHTEN));
    }

    @Test
    void sichten_aus_offen_wird_verweigert() {
        var def = FristenErinnerungProcess.DEFINITION;
        DomainException ex = assertThrows(DomainException.class, () ->
                def.nextStage(FristStatus.OFFEN, FristTrigger.SICHTEN));
        assertEquals("MDA-BPF-001", ex.code());
    }
}
