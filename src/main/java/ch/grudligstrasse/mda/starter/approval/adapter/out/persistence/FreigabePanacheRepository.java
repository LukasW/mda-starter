package ch.grudligstrasse.mda.starter.approval.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FreigabePanacheRepository implements PanacheRepositoryBase<FreigabeJpaEntity, UUID> {

    public List<FreigabeJpaEntity> findByVertrag(UUID vertragId) {
        return find("vertragId = ?1 order by angefordertAm desc", vertragId).list();
    }
}
