package ch.grudligstrasse.mda.clm.person.application.port.in;

import ch.grudligstrasse.mda.clm.person.domain.PersonId;

import java.util.UUID;

public interface PersonErfassenUseCase {

    PersonId execute(PersonErfassenCommand cmd);

    record PersonErfassenCommand(String vorname, String nachname, String email,
                                 String organisation, String funktion, UUID tenantId) {}
}
