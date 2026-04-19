package ch.grudligstrasse.mda.starter.obligation.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FristStatusTest {

    @Test
    void enum_werte_entsprechen_spezifikation() {
        assertEquals(4, FristStatus.values().length);
        assertEquals(FristStatus.OFFEN,     FristStatus.valueOf("OFFEN"));
        assertEquals(FristStatus.ERINNERT,  FristStatus.valueOf("ERINNERT"));
        assertEquals(FristStatus.ERLEDIGT,  FristStatus.valueOf("ERLEDIGT"));
        assertEquals(FristStatus.ESKALIERT, FristStatus.valueOf("ESKALIERT"));
    }
}
