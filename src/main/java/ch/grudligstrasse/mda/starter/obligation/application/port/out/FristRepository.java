package ch.grudligstrasse.mda.starter.obligation.application.port.out;

import ch.grudligstrasse.mda.starter.obligation.domain.Frist;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FristRepository {
    Frist save(Frist frist);
    Optional<Frist> findById(FristId id);
    List<Frist> findByVertrag(UUID vertragId);
    List<Frist> findFaelligeBis(LocalDate stichtag);
    List<Frist> findAll();
}
