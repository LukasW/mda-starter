package ch.grudligstrasse.mda.clm.shared.process;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BpfInstanceRepository implements PanacheRepositoryBase<BpfInstanceEntity, UUID> {

    public Optional<BpfInstanceEntity> findActive(String processName, UUID aggregateId) {
        return find("processName = ?1 and aggregateId = ?2 and completedAt is null",
                processName, aggregateId).firstResultOptional();
    }
}
