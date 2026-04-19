package ch.grudligstrasse.mda.starter.bdd.service;

import ch.grudligstrasse.mda.starter.contract.domain.MandantId;
import ch.grudligstrasse.mda.starter.contract.domain.PartnerId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragStatus;
import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VertragServiceSteps {

    private String titel;
    private Vertragsart vertragsart;
    private Vertrag vertrag;
    private DomainException lastError;

    @Angenommen("ein neuer Vertragsentwurf mit Titel {string} und Vertragsart {string}")
    public void ein_neuer_vertragsentwurf(String titel, String vertragsart) {
        this.titel = titel;
        this.vertragsart = Vertragsart.valueOf(vertragsart);
    }

    @Wenn("der Antragsteller den Vertrag erfasst")
    public void der_antragsteller_erfasst() {
        vertrag = Vertrag.erfassen(
                MandantId.of(UUID.randomUUID()),
                titel,
                vertragsart,
                PartnerId.of(UUID.randomUUID()),
                null, null, null,
                UserId.of(UUID.randomUUID()));
    }

    @Und("der Antragsteller hat den Vertrag erfasst")
    public void der_antragsteller_hat_erfasst() {
        der_antragsteller_erfasst();
    }

    @Wenn("der Antragsteller den Vertrag einreicht")
    public void der_antragsteller_reicht_ein() {
        try {
            vertrag.markiereEingereicht();
        } catch (DomainException ex) {
            this.lastError = ex;
        }
    }

    @Dann("ist der Vertrag im Status {string}")
    public void ist_der_vertrag_im_status(String erwartet) {
        assertNotNull(vertrag);
        assertEquals(VertragStatus.valueOf(erwartet), vertrag.status());
    }

    @Dann("liefert der Service einen Fehler mit Code {string}")
    public void liefert_fehler_mit_code(String code) {
        assertNotNull(lastError, "Es wurde kein Fehler ausgeloest");
        assertEquals(code, lastError.code());
    }
}
