package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.process.VertragStage;

public interface VertragTriggerUseCase {

    VertragStage execute(VertragTriggerCommand cmd);

    record VertragTriggerCommand(VertragId vertragId, String trigger, String actor) {}
}
