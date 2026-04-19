package ch.grudligstrasse.mda.starter.obligation.adapter.out.persistence;

import ch.grudligstrasse.mda.starter.obligation.application.port.out.FristRepository;
import ch.grudligstrasse.mda.starter.obligation.domain.Frist;
import ch.grudligstrasse.mda.starter.obligation.domain.FristId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FristPersistenceAdapter implements FristRepository {

    @Inject FristPanacheRepository repo;

    @Override
    @Transactional
    public Frist save(Frist f) {
        FristJpaEntity e = repo.findByIdOptional(f.id().value()).orElseGet(FristJpaEntity::new);
        e.id = f.id().value();
        e.vertragId = f.vertragId();
        e.art = f.art();
        e.faelligkeitsDatum = f.faelligkeitsDatum();
        e.vorlaufTage = f.vorlaufTage();
        e.erinnerungsDatum = f.erinnerungsDatum();
        e.status = f.status();
        e.verantwortlicherUserId = f.verantwortlicherUserId();
        repo.getEntityManager().merge(e);
        return f;
    }

    @Override
    public Optional<Frist> findById(FristId id) {
        return repo.findByIdOptional(id.value()).map(this::toDomain);
    }

    @Override
    public List<Frist> findByVertrag(UUID vertragId) {
        return repo.findByVertrag(vertragId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Frist> findFaelligeBis(LocalDate stichtag) {
        return repo.findFaelligeBis(stichtag).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Frist> findAll() {
        return repo.listAll().stream().map(this::toDomain).toList();
    }

    private Frist toDomain(FristJpaEntity e) {
        return Frist.rehydrate(FristId.of(e.id), e.vertragId, e.art, e.faelligkeitsDatum,
                e.vorlaufTage, e.erinnerungsDatum, e.status, e.verantwortlicherUserId);
    }
}
