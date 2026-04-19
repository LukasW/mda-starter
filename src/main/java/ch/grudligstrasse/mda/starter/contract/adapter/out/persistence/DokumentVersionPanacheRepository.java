package ch.grudligstrasse.mda.starter.contract.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DokumentVersionPanacheRepository implements PanacheRepositoryBase<DokumentVersionJpaEntity, UUID> {

    public List<DokumentVersionJpaEntity> findByVertrag(UUID vertragId) {
        return find("vertragId = ?1 order by versionNummer", vertragId).list();
    }
}
