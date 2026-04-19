package ch.grudligstrasse.mda.starter.contract.application.port.in;

import ch.grudligstrasse.mda.starter.contract.domain.MandantId;
import ch.grudligstrasse.mda.starter.contract.domain.PartnerId;
import ch.grudligstrasse.mda.starter.contract.domain.UserId;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;
import ch.grudligstrasse.mda.starter.contract.domain.Vertragsart;

import java.time.LocalDate;

public interface VertragErfassenUseCase {

    VertragId erfassen(Command cmd);

    record Command(
            MandantId mandantId,
            String titel,
            Vertragsart vertragsart,
            PartnerId partnerId,
            LocalDate startDatum,
            LocalDate endDatum,
            Integer kuendigungsfristTage,
            UserId antragstellerId
    ) {}
}
