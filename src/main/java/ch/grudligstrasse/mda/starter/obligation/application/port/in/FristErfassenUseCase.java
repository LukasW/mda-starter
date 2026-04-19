package ch.grudligstrasse.mda.starter.obligation.application.port.in;

import ch.grudligstrasse.mda.starter.obligation.domain.FristArt;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;

import java.time.LocalDate;
import java.util.UUID;

public interface FristErfassenUseCase {
    FristId erfassen(Command cmd);

    record Command(
            UUID vertragId,
            FristArt art,
            LocalDate faelligkeitsDatum,
            int vorlaufTage,
            UUID verantwortlicherId
    ) {}
}
