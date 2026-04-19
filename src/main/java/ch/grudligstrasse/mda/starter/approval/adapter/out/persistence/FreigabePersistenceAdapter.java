package ch.grudligstrasse.mda.starter.approval.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.approval.application.port.out.FreigabeRepository;
import ch.grudligstrasse.mda.starter.approval.domain.Freigabe;
import ch.grudligstrasse.mda.starter.approval.domain.FreigabeId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FreigabePersistenceAdapter implements FreigabeRepository {

    @Inject FreigabePanacheRepository repo;

    @Override
    @Transactional
    public Freigabe save(Freigabe f) {
        FreigabeJpaEntity e = repo.findByIdOptional(f.id().value()).orElseGet(FreigabeJpaEntity::new);
        e.id = f.id().value();
        e.vertragId = f.vertragId();
        e.versionId = f.versionId();
        e.reviewerUserId = f.reviewerUserId();
        e.entscheidung = f.entscheidung().orElse(null);
        e.begruendung = f.begruendung().orElse(null);
        e.entschiedenAm = f.entschiedenAm().orElse(null);
        e.angefordertAm = f.angefordertAm();
        repo.getEntityManager().merge(e);
        return f;
    }

    @Override
    public Optional<Freigabe> findById(FreigabeId id) {
        return repo.findByIdOptional(id.value()).map(this::toDomain);
    }

    @Override
    public List<Freigabe> findByVertrag(UUID vertragId) {
        return repo.findByVertrag(vertragId).stream().map(this::toDomain).toList();
    }

    private Freigabe toDomain(FreigabeJpaEntity e) {
        return Freigabe.rehydrate(FreigabeId.of(e.id), e.vertragId, e.versionId, e.reviewerUserId,
                e.entscheidung, e.begruendung, e.entschiedenAm, e.angefordertAm);
    }
}
