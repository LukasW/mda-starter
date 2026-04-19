package ch.grudligstrasse.mda.clm.person.application.port.in;

import ch.grudligstrasse.mda.clm.person.domain.PersonId;

public interface PersonLoeschenUseCase {

    void execute(PersonLoeschenCommand cmd);

    record PersonLoeschenCommand(PersonId id, long expectedVersion) {}
}
