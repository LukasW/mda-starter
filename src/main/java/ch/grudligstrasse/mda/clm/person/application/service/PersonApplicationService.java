package ch.grudligstrasse.mda.clm.person.application.service;

import ch.grudligstrasse.mda.clm.person.application.port.in.PersonAendernUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonErfassenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonLoeschenUseCase;
import ch.grudligstrasse.mda.clm.person.application.port.in.PersonSuchenQuery;
import ch.grudligstrasse.mda.clm.person.application.port.out.ExternePersonenverwaltungClient;
import ch.grudligstrasse.mda.clm.person.application.port.out.PersonRepository;
import ch.grudligstrasse.mda.clm.person.domain.Email;
import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.person.domain.PersonId;
import ch.grudligstrasse.mda.clm.shared.problem.DomainException;
import ch.grudligstrasse.mda.clm.shared.process.BpfService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PersonApplicationService implements PersonErfassenUseCase, PersonSuchenQuery,
        PersonAendernUseCase, PersonLoeschenUseCase {

    private final PersonRepository repository;
    private final ExternePersonenverwaltungClient externerClient;

    public PersonApplicationService(PersonRepository repository,
                                    ExternePersonenverwaltungClient externerClient) {
        this.repository = repository;
        this.externerClient = externerClient;
    }

    @Override
    @Transactional
    public PersonId execute(PersonErfassenCommand cmd) {
        UUID tenant = cmd.tenantId() != null ? cmd.tenantId()
                : UUID.fromString(BpfService.TENANT_DEFAULT_UUID);
        Person person = Person.erfassen(cmd.vorname(), cmd.nachname(),
                new Email(cmd.email()), cmd.organisation(), cmd.funktion(), tenant);
        repository.save(person);
        return person.id();
    }

    @Override
    public List<Person> suchen(String query, int limit) {
        List<Person> lokal = repository.search(query, limit);
        if (!externerClient.isEnabled()) {
            return lokal;
        }
        List<Person> merged = new ArrayList<>(lokal);
        UUID tenant = UUID.fromString(BpfService.TENANT_DEFAULT_UUID);
        for (ExternePersonenverwaltungClient.ExternePerson ep : externerClient.suchen(query, limit)) {
            try {
                Person snapshot = Person.snapshotExtern(ep.externeId(), ep.vorname(), ep.nachname(),
                        new Email(ep.email()), ep.organisation(), ep.funktion(), tenant);
                merged.add(snapshot);
            } catch (IllegalArgumentException ignored) {
                // Defensive: ungueltige Daten aus externer Quelle werden ignoriert.
            }
        }
        return merged;
    }

    @Override
    public Optional<Person> byId(PersonId id) {
        return repository.findById(id);
    }

    // mda-generator: manual-edits-below

    @Override
    @Transactional
    public void execute(PersonAendernCommand cmd) {
        Person person = repository.findById(cmd.id())
                .orElseThrow(() -> DomainException.notFound("MDA-PER-404",
                        "Person nicht gefunden: " + cmd.id().asString()));
        try {
            person.aktualisiereSicher(cmd.vorname(), cmd.nachname(),
                    new Email(cmd.email()), cmd.organisation(), cmd.funktion(), cmd.expectedVersion());
        } catch (Person.PersonReadOnlyException e) {
            throw DomainException.unprocessable("MDA-PER-002", e.getMessage());
        } catch (Person.PersonVersionKonfliktException e) {
            throw DomainException.conflict("MDA-PER-409", e.getMessage());
        }
        repository.save(person);
    }

    @Override
    @Transactional
    public void execute(PersonLoeschenCommand cmd) {
        Person person = repository.findById(cmd.id())
                .orElseThrow(() -> DomainException.notFound("MDA-PER-404",
                        "Person nicht gefunden: " + cmd.id().asString()));
        try {
            person.loeschen(cmd.expectedVersion());
        } catch (Person.PersonReadOnlyException e) {
            throw DomainException.unprocessable("MDA-PER-003", e.getMessage());
        } catch (Person.PersonVersionKonfliktException e) {
            throw DomainException.conflict("MDA-PER-409", e.getMessage());
        }
        repository.softDelete(person);
    }
}
