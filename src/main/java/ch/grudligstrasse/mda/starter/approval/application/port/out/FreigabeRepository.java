package ch.grudligstrasse.mda.starter.approval.application.port.out;

import ch.grudligstrasse.mda.starter.approval.domain.Freigabe;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FreigabeRepository {
    Freigabe save(Freigabe freigabe);
    Optional<Freigabe> findById(FreigabeId id);
    List<Freigabe> findByVertrag(UUID vertragId);
}
