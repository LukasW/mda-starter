package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonenQuelleTest {

    @Test
    void enthaeltZweiAuspraegungen() {
        assertEquals(2, PersonenQuelle.values().length);
    }

    @Test
    void valueOf_unterscheidetIntern_und_externApi() {
        assertEquals(PersonenQuelle.INTERN, PersonenQuelle.valueOf("INTERN"));
        assertEquals(PersonenQuelle.EXTERN_API, PersonenQuelle.valueOf("EXTERN_API"));
        assertNotEquals(PersonenQuelle.INTERN, PersonenQuelle.EXTERN_API);
    }

    @Test
    void valueOf_wirftBeiUnbekanntemNamen() {
        assertThrows(IllegalArgumentException.class, () -> PersonenQuelle.valueOf("UNBEKANNT"));
    }
}
