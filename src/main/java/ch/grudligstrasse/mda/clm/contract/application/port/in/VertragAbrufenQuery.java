package ch.grudligstrasse.mda.clm.contract.application.port.in;

import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VertragAbrufenQuery {

    Optional<Vertrag> byId(VertragId id);

    List<Vertrag> byTenant(UUID tenantId, int top, int skip);
}
