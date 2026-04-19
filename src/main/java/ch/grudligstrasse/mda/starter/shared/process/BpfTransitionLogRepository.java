package ch.grudligstrasse.mda.starter.shared.process;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BpfTransitionLogRepository implements PanacheRepositoryBase<BpfTransitionLogEntity, UUID> {

    public List<BpfTransitionLogEntity> findByInstance(UUID instanceId) {
        return find("instanceId = ?1 order by occurredAt", instanceId).list();
    }
}
