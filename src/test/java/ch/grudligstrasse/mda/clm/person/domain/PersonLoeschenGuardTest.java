package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonLoeschenGuardTest {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void loeschen_setztDeletedAtUndBumptVersion() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);
        long vor = p.versionNumber();

        assertFalse(p.istGeloescht());
        p.loeschen(vor);

        assertTrue(p.istGeloescht());
        assertNotNull(p.deletedAt());
        assertEquals(vor + 1, p.versionNumber());
    }

    @Test
    void loeschen_idempotent_zweimalKeinFehler() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);
        p.loeschen(p.versionNumber());
        Instant ersterStempel = p.deletedAt();

        p.loeschen(p.versionNumber());

        assertEquals(ersterStempel, p.deletedAt());
    }

    @Test
    void loeschen_wirftBeiExternSnapshot() {
        Person extern = Person.rehydrate(PersonId.generate(), TENANT, "Ext", "Ern",
                new Email("e@ex.ch"), null, null,
                PersonenQuelle.EXTERN_API, "EXT-1",
                Instant.now(), Instant.now(), null, 0);

        assertThrows(Person.PersonReadOnlyException.class, () -> extern.loeschen(0));
    }

    @Test
    void loeschen_wirftBeiVersionKonflikt() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);

        assertThrows(Person.PersonVersionKonfliktException.class,
                () -> p.loeschen(p.versionNumber() + 99));
    }
}
