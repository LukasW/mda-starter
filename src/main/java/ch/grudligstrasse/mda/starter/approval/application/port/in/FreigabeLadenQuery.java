package ch.grudligstrasse.mda.starter.approval.application.port.in;

import ch.grudligstrasse.mda.starter.approval.domain.Freigabe;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreigabeLadenQuery {
    Optional<Freigabe> laden(FreigabeId id);
    List<Freigabe> fuerVertrag(UUID vertragId);
}
