package ch.grudligstrasse.mda.clm.person.adapter.out.persistence;

import ch.grudligstrasse.mda.clm.person.application.port.out.PersonRepository;
import ch.grudligstrasse.mda.clm.person.domain.Email;
import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;
import ch.grudligstrasse.mda.clm.person.domain.PersonenQuelle;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PersonPanacheRepository implements
        PanacheRepositoryBase<PersonJpaEntity, UUID>,
        PersonRepository {

    @Override
    public void save(Person person) {
        PersonJpaEntity existing = findById(person.id().value());
        PersonJpaEntity target = existing != null ? existing : new PersonJpaEntity();
        target.id = person.id().value();
        target.tenantId = person.tenantId();
        target.vorname = person.vorname();
        target.nachname = person.nachname();
        target.email = person.email().value();
        target.organisation = person.organisation();
        target.funktion = person.funktion();
        target.quelleTyp = person.quelleTyp().name();
        target.externeId = person.externeId();
        target.erstelltAm = person.erstelltAm();
        target.modifiedAt = person.modifiedAt();
        if (existing == null) {
            persist(target);
        }
    }

    @Override
    public Optional<Person> findById(PersonId id) {
        return Optional.ofNullable(findById(id.value())).map(PersonPanacheRepository::toDomain);
    }

    @Override
    public List<Person> search(String query, int limit) {
        int size = Math.max(limit, 1);
        String term = query == null || query.isBlank() ? "%" : "%" + query.toLowerCase() + "%";
        return find("""
                lower(vorname) like ?1 or lower(nachname) like ?1
                or lower(email) like ?1 or lower(coalesce(organisation,'')) like ?1
                """, term)
                .page(0, size)
                .list()
                .stream()
                .map(PersonPanacheRepository::toDomain)
                .toList();
    }

    private static Person toDomain(PersonJpaEntity e) {
        return Person.rehydrate(
                new PersonId(e.id), e.tenantId, e.vorname, e.nachname,
                new Email(e.email), e.organisation, e.funktion,
                PersonenQuelle.valueOf(e.quelleTyp), e.externeId,
                e.erstelltAm, e.modifiedAt, e.versionNumber);
    }
}
