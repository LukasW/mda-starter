package ch.grudligstrasse.mda.starter.contract.application.port.out;

import ch.grudligstrasse.mda.starter.contract.domain.Vertrag;
import ch.grudligstrasse.mda.starter.contract.domain.VertragId;

import java.util.List;
import java.util.Optional;

public interface VertragRepository {
    Vertrag save(Vertrag vertrag);
    Optional<Vertrag> findById(VertragId id);
    List<Vertrag> findAll();
}
