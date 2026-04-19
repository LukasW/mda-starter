package ch.grudligstrasse.mda.clm.bdd.service;

import ch.grudligstrasse.mda.clm.person.application.port.out.PersonRepository;
import ch.grudligstrasse.mda.clm.person.domain.Email;
import ch.grudligstrasse.mda.clm.person.domain.Person;
import ch.grudligstrasse.mda.clm.shared.process.BpfService;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import io.quarkiverse.cucumber.ScenarioScope;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ScenarioScope
public class PersonServiceSteps {

    @Inject
    PersonRepository personRepository;

    private String personId;
    private Response letzteResponse;

    @Wenn("ich eine Person mit Vorname {string} Nachname {string} Email {string} erfasse")
    public void ichErfasseEinePerson(String vorname, String nachname, String email) {
        letzteResponse = given()
                .contentType("application/json")
                .body(Map.of("vorname", vorname, "nachname", nachname, "email", email))
                .post("/api/v1/personen");
        if (letzteResponse.statusCode() == 201) {
            personId = letzteResponse.jsonPath().getString("id");
        }
    }

    @Dann("wird die Person mit Versionsnummer {int} angelegt")
    public void wirdDiePersonMitVersionsnummerAngelegt(int erwartet) {
        assertEquals(201, letzteResponse.statusCode());
        given().get("/api/v1/personen/" + personId)
                .then().statusCode(200)
                .body("versionNumber", org.hamcrest.Matchers.equalTo(erwartet));
    }

    @Wenn("ich den Nachname der Person auf {string} mit erwarteter Version {int} ändere")
    public void ichAendereDenNachname(String nachname, int expectedVersion) {
        Map<String, Object> body = new HashMap<>();
        body.put("vorname", "Sara");
        body.put("nachname", nachname);
        body.put("email", "sara.beispiel@example.ch");
        body.put("expectedVersion", expectedVersion);
        letzteResponse = given()
                .contentType("application/json")
                .body(body)
                .put("/api/v1/personen/" + personId);
    }

    @Dann("hat die Person den Nachname {string} und Version {int}")
    public void hatDiePersonDenNachnameUndVersion(String nachname, int version) {
        assertEquals(204, letzteResponse.statusCode());
        given().get("/api/v1/personen/" + personId)
                .then().statusCode(200)
                .body("nachname", org.hamcrest.Matchers.equalTo(nachname))
                .body("versionNumber", org.hamcrest.Matchers.equalTo(version));
    }

    @Angenommen("es existiert eine externe Personen-Snapshot-Person")
    @Transactional
    public void esExistiertEineExterneSnapshotPerson() {
        UUID tenant = UUID.fromString(BpfService.TENANT_DEFAULT_UUID);
        Person extern = Person.snapshotExtern(
                "EXT-" + UUID.randomUUID(),
                "Ex", "Tern",
                new Email("extern." + UUID.randomUUID() + "@example.ch"),
                "Ex AG", "CTO", tenant);
        personRepository.save(extern);
        personId = extern.id().asString();
    }

    @Wenn("ich die externe Person löschen möchte")
    public void ichLoescheDieExternePerson() {
        letzteResponse = given().delete("/api/v1/personen/" + personId + "?expectedVersion=0");
    }

    @Dann("erhalte ich beim Löschen einen Fehler mit Code {string}")
    public void erhalteIchBeimLoeschenEinenFehlerMitCode(String code) {
        assertEquals(422, letzteResponse.statusCode());
        assertEquals(code, letzteResponse.jsonPath().getString("code"));
        assertEquals(422, letzteResponse.jsonPath().getInt("status"));
    }
}
