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

    @Test
    void neueErfassung_istNichtGeloescht() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);
        org.junit.jupiter.api.Assertions.assertNull(p.deletedAt());
        org.junit.jupiter.api.Assertions.assertFalse(p.istGeloescht());
        org.junit.jupiter.api.Assertions.assertTrue(p.istIntern());
    }

    @Test
    void snapshotExtern_setztQuelletypUndExterneId() {
        Person p = Person.snapshotExtern("EXT-42", "Anna", "Beispiel",
                new Email("a@b.ch"), "AG", "CFO", TENANT);

        assertEquals(PersonenQuelle.EXTERN_API, p.quelleTyp());
        assertEquals("EXT-42", p.externeId());
        org.junit.jupiter.api.Assertions.assertFalse(p.istIntern());
    }

    @Test
    void rehydrate_mitDeletedAtErhaeltStempel() {
        java.time.Instant geloescht = java.time.Instant.parse("2026-04-01T12:00:00Z");
        Person p = Person.rehydrate(PersonId.generate(), TENANT, "X", "Y",
                new Email("x@y.ch"), null, null,
                PersonenQuelle.INTERN, null,
                java.time.Instant.now(), java.time.Instant.now(), geloescht, 7);

        assertEquals(geloescht, p.deletedAt());
        assertEquals(7, p.versionNumber());
        org.junit.jupiter.api.Assertions.assertTrue(p.istGeloescht());
    }
}
