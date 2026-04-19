package ch.grudligstrasse.mda.starter.contract.application.port.in;

import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.util.List;
import java.util.Optional;

public interface VertragLadenQuery {
    Optional<Vertrag> laden(VertragId id);
    List<Vertrag> alle();
}
