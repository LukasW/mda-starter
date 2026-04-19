package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.ParteiRolle;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.util.UUID;

public interface VertragPersonZuordnenUseCase {

    void execute(VertragPersonZuordnenCommand cmd);

    record VertragPersonZuordnenCommand(VertragId vertragId, UUID personId, ParteiRolle rolle) {}
}
