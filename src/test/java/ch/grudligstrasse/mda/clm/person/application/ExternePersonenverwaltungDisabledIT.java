package ch.grudligstrasse.mda.clm.person.application;

import ch.grudligstrasse.mda.clm.person.adapter.out.persistence.DisabledExternePersonenverwaltungClient;
import ch.grudligstrasse.mda.clm.person.application.port.out.ExternePersonenverwaltungClient;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifiziert, dass im Standalone-Modus (Default) die {@link DisabledExternePersonenverwaltungClient}
 * aktiv ist und keine externen Personen liefert.
 */
@QuarkusTest
class ExternePersonenverwaltungDisabledIT {

    @Inject
    ExternePersonenverwaltungClient client;

    @Test
    void istStandardmaessigDeaktiviert() {
        assertInstanceOf(DisabledExternePersonenverwaltungClient.class, client,
                "Erwartet wird der Disabled-Default-Adapter im Standalone-Modus.");
        assertFalse(client.isEnabled());
    }

    @Test
    void liefertImmerLeereListe() {
        assertTrue(client.suchen("egal", 10).isEmpty());
        assertTrue(client.suchen("", 10).isEmpty());
    }
}
