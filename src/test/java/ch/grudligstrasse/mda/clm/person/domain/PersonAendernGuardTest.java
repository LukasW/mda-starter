package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonAendernGuardTest {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void aktualisiereSicher_bumptVersion() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);
        long vor = p.versionNumber();

        p.aktualisiereSicher("Anna", "Neu", new Email("a@b.ch"), null, null, vor);

        assertEquals(vor + 1, p.versionNumber());
        assertEquals("Neu", p.nachname());
    }

    @Test
    void aktualisiereSicher_wirftBeiVersionKonflikt() {
        Person p = Person.erfassen("Anna", "Beispiel", new Email("a@b.ch"), null, null, TENANT);

        assertThrows(Person.PersonVersionKonfliktException.class,
                () -> p.aktualisiereSicher("Anna", "Anders", new Email("a@b.ch"),
                        null, null, p.versionNumber() + 99));
    }

    @Test
    void aktualisiereSicher_wirftBeiExternSnapshot() {
        Person extern = Person.rehydrate(PersonId.generate(), TENANT, "Ext", "Ern",
                new Email("e@ex.ch"), "X", null,
                PersonenQuelle.EXTERN_API, "EXT-1",
                Instant.now(), Instant.now(), null, 0);

        assertThrows(Person.PersonReadOnlyException.class,
                () -> extern.aktualisiereSicher("Ext", "Neu", new Email("e@ex.ch"),
                        null, null, 0));
    }
}
