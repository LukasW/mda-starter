package ch.grudligstrasse.mda.clm.contract.application;

import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragErstellenUseCase;
import ch.grudligstrasse.mda.clm.contract.application.port.in.VertragTriggerUseCase;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsTyp;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class VertragApplicationServiceIT {

    @Inject
    VertragErstellenUseCase erstellen;

    @Inject
    VertragTriggerUseCase triggern;

    @Test
    void erstellenAnschliessendEinreichen_aendertStageUndPersistiert() {
        VertragId id = erstellen.execute(new VertragErstellenUseCase.VertragErstellenCommand(
                "IT-Vertrag", VertragsTyp.KUNDENVERTRAG, UUID.randomUUID(), null));
        VertragStage stage = triggern.execute(new VertragTriggerUseCase.VertragTriggerCommand(
                id, "einreichen", "it-test"));
        assertEquals(VertragStage.IN_PRUEFUNG, stage);
    }

    @Test
    void unerlaubterTriggerWirftDomainException() {
        VertragId id = erstellen.execute(new VertragErstellenUseCase.VertragErstellenCommand(
                "Fehlercase", VertragsTyp.SONSTIGES, UUID.randomUUID(), null));
        DomainException ex = assertThrows(DomainException.class,
                () -> triggern.execute(new VertragTriggerUseCase.VertragTriggerCommand(
                        id, "unterzeichnen", "it")));
        assertEquals("MDA-BPF-001", ex.code());
    }
}
