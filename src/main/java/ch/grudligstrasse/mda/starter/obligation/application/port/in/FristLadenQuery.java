package ch.grudligstrasse.mda.starter.obligation.application.port.in;

import ch.grudligstrasse.mda.starter.obligation.domain.Frist;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FristLadenQuery {
    Optional<Frist> laden(FristId id);
    List<Frist> fuerVertrag(UUID vertragId);
    List<Frist> alle();
}
