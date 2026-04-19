package ch.grudligstrasse.mda.clm.bdd.service;

import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Gegebensei;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import io.quarkiverse.cucumber.ScenarioScope;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ScenarioScope
public class VertragServiceSteps {

    private String vertragId;
    private Response letzteResponse;
    private UUID aktuellerUser;

    @Gegebensei("ein angemeldeter Sachbearbeiter")
    public void einAngemeldeterSachbearbeiter() {
        aktuellerUser = UUID.randomUUID();
    }

    @Wenn("ich einen Vertrag mit Titel {string} vom Typ {string} erstelle")
    public void ichErstelleEinenVertrag(String titel, String typ) {
        if (aktuellerUser == null) {
            aktuellerUser = UUID.randomUUID();
        }
        letzteResponse = given()
                .contentType("application/json")
                .body(Map.of(
                        "titel", titel,
                        "typ", typ,
                        "erstellerId", aktuellerUser.toString()))
                .post("/api/v1/vertraege");
        if (letzteResponse.statusCode() == 201) {
            String location = letzteResponse.header("Location");
            vertragId = location.substring(location.lastIndexOf('/') + 1);
        }
    }

    @Dann("wird der Vertrag mit Status {string} angelegt")
    public void wirdDerVertragMitStatusAngelegt(String status) {
        assertEquals(201, letzteResponse.statusCode());
        given().get("/api/v1/vertraege/" + vertragId)
                .then().statusCode(200).body("stage", equalTo(status));
    }

    @Und("eine VertragId wird zurückgegeben")
    public void eineVertragIdWirdZurueckgegeben() {
        given().get("/api/v1/vertraege/" + vertragId)
                .then().body("id", notNullValue());
    }

    @Gegebensei("ein Vertrag im Status {string} mit Titel {string}")
    public void einVertragImStatusMitTitel(String status, String titel) {
        aktuellerUser = UUID.randomUUID();
        ichErstelleEinenVertrag(titel, "SONSTIGES");
        assertEquals(status, "ENTWURF");
    }

    @Wenn("der Titel auf {string} geändert wird")
    public void derTitelAufGeaendertWird(String neuerTitel) {
        assertNotNull(vertragId);
        given().contentType("application/json")
                .body(Map.of("titel", neuerTitel))
                .put("/api/v1/vertraege/" + vertragId + "/metadaten")
                .then().statusCode(204);
    }

    @Dann("trägt der Vertrag den Titel {string}")
    public void traegtDerVertragDenTitel(String titel) {
        given().get("/api/v1/vertraege/" + vertragId)
                .then().statusCode(200).body("titel", equalTo(titel));
    }

    @Gegebensei("ein Vertrag im Status {string}")
    public void einVertragImStatus(String status) {
        einVertragImStatusMitTitel(status, "BDD-Vertrag");
    }

    @Wenn("der Trigger {string} ausgeloest wird")
    public void derTriggerAusgeloestWird(String trigger) {
        letzteResponse = given()
                .post("/api/v1/vertraege/" + vertragId + "/process/contract/trigger/" + trigger);
    }

    @Dann("ist die Stage {string}")
    public void istDieStage(String stage) {
        assertEquals(200, letzteResponse.statusCode());
        assertEquals(stage, letzteResponse.jsonPath().getString("stage"));
    }

    @Dann("erhalte ich einen Fehler mit Code {string}")
    public void erhalteIchEinenFehlerMitCode(String code) {
        assertEquals(422, letzteResponse.statusCode());
        assertEquals(code, letzteResponse.jsonPath().getString("code"));
    }
}
