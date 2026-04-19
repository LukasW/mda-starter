package ch.grudligstrasse.mda.clm.person.application;

import ch.grudligstrasse.mda.clm.person.application.port.in.PersonErfassenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonSuchenQuery;
import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class PersonApplicationServiceIT {

    @Inject
    PersonErfassenUseCase erfassen;

    @Inject
    PersonSuchenQuery suche;

    @Test
    void erfassenUndDanachFinden() {
        PersonId id = erfassen.execute(new PersonErfassenUseCase.PersonErfassenCommand(
                "Anna", "Integration", "anna.integration@example.ch", "AG", "CIO", null));

        Optional<Person> found = suche.byId(id);
        assertTrue(found.isPresent());
        assertEquals("Anna", found.get().vorname());
    }

    @Test
    void sucheFindetNachNachname() {
        erfassen.execute(new PersonErfassenUseCase.PersonErfassenCommand(
                "Sucher", "Meier", "suchmeier@example.ch", null, null, null));
        List<Person> treffer = suche.suchen("meier", 10);
        assertFalse(treffer.isEmpty());
    }
}
