package ch.grudligstrasse.mda.clm.person.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersonIdTest {

    @Test
    void parse_ueberlebtRoundtrip() {
        UUID raw = UUID.randomUUID();
        PersonId id = PersonId.parse(raw.toString());
        assertEquals(raw, id.value());
        assertEquals(raw.toString(), id.asString());
    }

    @Test
    void parse_wirftBeiUngueltigerUuid() {
        assertThrows(IllegalArgumentException.class, () -> PersonId.parse("kein-uuid"));
    }

    @Test
    void generate_liefertDistinkteIds() {
        PersonId a = PersonId.generate();
        PersonId b = PersonId.generate();
        assertNotNull(a.value());
        assertNotNull(b.value());
        assertEquals(false, a.equals(b));
    }
}
