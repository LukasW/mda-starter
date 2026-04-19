package ch.grudligstrasse.mda.clm.person.application.port.out;

import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    void save(Person person);

    Optional<Person> findById(PersonId id);

    List<Person> search(String query, int limit);

    /** Soft-Delete: setzt {@code deleted_at} via {@link Person#loeschen(long)}. */
    void softDelete(Person person);
}
