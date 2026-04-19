package ch.grudligstrasse.mda.clm.contract.application.port.out;

import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VertragRepository {

    void save(Vertrag vertrag);

    Optional<Vertrag> findById(VertragId id);

    List<Vertrag> findByTenant(UUID tenantId, int top, int skip);
}
