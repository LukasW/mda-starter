package ch.grudligstrasse.mda.starter.bdd.process;

import ch.grudligstrasse.mda.starter.contract.domain.VertragLifecycle;
import ch.grudligstrasse.mda.starter.contract.domain.VertragStatus;
import ch.grudligstrasse.mda.starter.contract.domain.VertragTrigger;
import ch.grudligstrasse.mda.starter.shared.problem.DomainException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VertragLifecycleSteps {

    private VertragStatus currentStage;
    private DomainException lastError;

    @Angenommen("eine BPF-Instanz fuer Vertrag im Stage {string}")
    public void eine_bpf_instanz(String stage) {
        this.currentStage = VertragStatus.valueOf(stage);
        this.lastError = null;
    }

    @Wenn("der Trigger {string} ausgeloest wird")
    public void der_trigger_wird_ausgeloest(String trigger) {
        try {
            currentStage = VertragLifecycle.DEFINITION.nextStage(
                    currentStage, VertragTrigger.valueOf(trigger));
        } catch (DomainException ex) {
            lastError = ex;
        }
    }

    @Dann("ist der Stage {string}")
    public void ist_der_stage(String stage) {
        assertEquals(VertragStatus.valueOf(stage), currentStage);
    }

    @Dann("liefert das BPF einen Fehler mit Code {string}")
    public void liefert_bpf_fehler(String code) {
        assertNotNull(lastError, "Es wurde kein Fehler ausgeloest");
        assertEquals(code, lastError.code());
    }
}
