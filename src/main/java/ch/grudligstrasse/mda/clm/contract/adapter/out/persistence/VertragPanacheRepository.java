package ch.grudligstrasse.mda.clm.contract.adapter.out.persistence;

import ch.grudligstrasse.mda.clm.contract.application.port.out.VertragRepository;
import ch.grudligstrasse.mda.clm.contract.domain.Vertrag;
import ch.grudligstrasse.mda.clm.contract.domain.VertragId;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class VertragPanacheRepository implements
        PanacheRepositoryBase<VertragJpaEntity, UUID>,
        VertragRepository {

    @Override
    public void save(Vertrag vertrag) {
        VertragJpaEntity existing = findById(vertrag.id().value());
        if (existing == null) {
            VertragJpaEntity neu = VertragMapper.toEntity(vertrag, null);
            persist(neu);
        } else {
            VertragMapper.toEntity(vertrag, existing);
        }
    }

    @Override
    public Optional<Vertrag> findById(VertragId id) {
        VertragJpaEntity entity = findById(id.value());
        return Optional.ofNullable(entity).map(VertragMapper::toDomain);
    }

    @Override
    public List<Vertrag> findByTenant(UUID tenantId, int top, int skip) {
        int pageSize = Math.max(top, 1);
        int pageIndex = Math.max(skip / pageSize, 0);
        return find("tenantId = ?1 order by erstelltAm desc", tenantId)
                .page(pageIndex, pageSize)
                .list()
                .stream()
                .map(VertragMapper::toDomain)
                .toList();
    }
}
