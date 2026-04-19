package ch.grudligstrasse.mda.clm.person.application.port.in;

import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;

import java.util.List;
import java.util.Optional;

public interface PersonSuchenQuery {

    List<Person> suchen(String query, int limit);

    Optional<Person> byId(PersonId id);
}
