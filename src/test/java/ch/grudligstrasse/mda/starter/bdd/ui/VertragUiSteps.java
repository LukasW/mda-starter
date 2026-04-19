package ch.grudligstrasse.mda.starter.bdd.ui;

import ch.grudligstrasse.mda.starter.contract.domain.MandantId;
import ch.grudligstrasse.mda.starter.contract.domain.PartnerId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragStatus;
import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VertragUiSteps {

    private String mode = System.getProperty("MDA_UI_MODE", "rest");
    private Vertrag vertrag;

    @Angenommen("der Benutzer offnet die Vertragserfassung")
    public void benutzer_oeffnet_erfassung() {
        if ("playwright".equalsIgnoreCase(mode)) {
            throw new org.opentest4j.TestAbortedException(
                    "Playwright-Modus im MVP-Starter nicht aktiviert. MDA_UI_MODE=rest verwenden.");
        }
    }

    @Wenn("der Benutzer einen neuen Vertrag mit Titel {string} und Vertragsart {string} erfasst")
    public void benutzer_erfasst_vertrag(String titel, String art) {
        vertrag = Vertrag.erfassen(
                MandantId.of(UUID.randomUUID()),
                titel,
                Vertragsart.valueOf(art),
                PartnerId.of(UUID.randomUUID()),
                null, null, null,
                UserId.of(UUID.randomUUID()));
    }

    @Dann("ist der Vertrag in der Uebersicht im Status {string} sichtbar")
    public void vertrag_ist_sichtbar(String erwartet) {
        assertNotNull(vertrag);
        assertEquals(VertragStatus.valueOf(erwartet), vertrag.status());
    }
}
