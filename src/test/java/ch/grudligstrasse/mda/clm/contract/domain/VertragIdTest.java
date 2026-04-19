package ch.grudligstrasse.mda.clm.contract.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VertragIdTest {

    @Test
    void generateLieferUnterschiedlicheIds() {
        assertNotNull(VertragId.generate());
        assertNotNull(VertragId.generate().value());
    }

    @Test
    void parseRundtrip() {
        UUID uuid = UUID.randomUUID();
        VertragId id = VertragId.parse(uuid.toString());
        assertEquals(uuid, id.value());
        assertEquals(uuid.toString(), id.asString());
    }

    @Test
    void parseInvalidTriggerException() {
        assertThrows(IllegalArgumentException.class, () -> VertragId.parse("not-a-uuid"));
    }

    @Test
    void nullValueWirftNpe() {
        assertThrows(NullPointerException.class, () -> new VertragId(null));
    }
}
