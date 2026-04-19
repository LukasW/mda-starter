package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void akzeptiertGueltigeAdresseUndNormalisiert() {
        Email e = new Email("  Foo.Bar@Example.CH ");
        assertEquals("foo.bar@example.ch", e.value());
    }

    @Test
    void lehntLeerzeichenAb() {
        assertThrows(IllegalArgumentException.class, () -> new Email("foo bar@example.ch"));
    }

    @Test
    void lehntFehlendesAtAb() {
        assertThrows(IllegalArgumentException.class, () -> new Email("noAtSymbol.ch"));
    }

    @Test
    void lehntNullAb() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }
}
