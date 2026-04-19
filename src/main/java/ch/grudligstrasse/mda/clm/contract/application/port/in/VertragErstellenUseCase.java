package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import ch.grudligstrasse.mda.clm.contract.domain.VertragsTyp;

import java.util.UUID;

public interface VertragErstellenUseCase {

    VertragId execute(VertragErstellenCommand cmd);

    record VertragErstellenCommand(String titel, VertragsTyp typ, UUID erstellerId, UUID tenantId) {}
}
