package ch.grudligstrasse.mda.clm.person.application.port.in;

import ch.grudligstrasse.mda.clm.person.domain.PersonId;

public interface PersonAendernUseCase {

    void execute(PersonAendernCommand cmd);

    record PersonAendernCommand(PersonId id, String vorname, String nachname, String email,
                                String organisation, String funktion, long expectedVersion) {}
}
