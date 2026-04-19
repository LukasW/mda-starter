package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonTest {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void erfassen_erzeugtInternePersonMitNormalisierterEmail() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("Anna@BSP.ch"),
                "AG", "CEO", TENANT);

        assertEquals("anna@bsp.ch", p.email().value());
        assertEquals(PersonenQuelle.INTERN, p.quelleTyp());
    }

    @Test
    void externSnapshot_verlangtExterneId() {
        assertThrows(IllegalArgumentException.class,
                () -> Person.rehydrate(PersonId.generate(), TENANT, "X", "Y",
                        new Email("x@y.ch"), null, null,
                        PersonenQuelle.EXTERN_API, null,
                        java.time.Instant.now(), java.time.Instant.now(), 0));
    }

    @Test
    void emailFormat_wirdValidiert() {
        assertThrows(IllegalArgumentException.class, () -> new Email("kein-email"));
    }

    @Test
    void aktualisieren_bumptVersion() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);
        long before = p.versionNumber();
        p.aktualisieren("Anna", "Neu", new Email("a@b.ch"), null, null);
        assertEquals(before + 1, p.versionNumber());
    }
}
