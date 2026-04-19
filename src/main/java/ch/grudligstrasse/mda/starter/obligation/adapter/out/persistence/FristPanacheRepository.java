package ch.grudligstrasse.mda.starter.obligation.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.obligation.domain.FristStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FristPanacheRepository implements PanacheRepositoryBase<FristJpaEntity, UUID> {

    public List<FristJpaEntity> findByVertrag(UUID vertragId) {
        return find("vertragId = ?1 order by faelligkeitsDatum", vertragId).list();
    }

    public List<FristJpaEntity> findFaelligeBis(LocalDate stichtag) {
        return find("status = ?1 and erinnerungsDatum <= ?2",
                FristStatus.OFFEN, stichtag).list();
    }
}
