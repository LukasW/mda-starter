package ch.grudligstrasse.mda.starter.shared.process;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BpfInstanceRepository implements PanacheRepositoryBase<BpfInstanceEntity, UUID> {

    public Optional<BpfInstanceEntity> findForAggregate(String processName, String aggregateType, UUID aggregateId) {
        return find("processName = ?1 and aggregateType = ?2 and aggregateId = ?3",
                processName, aggregateType, aggregateId).firstResultOptional();
    }
}
