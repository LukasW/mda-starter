package ch.grudligstrasse.mda.clm.bdd.process;

import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Gegebensei;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import io.quarkiverse.cucumber.ScenarioScope;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ScenarioScope
public class VertragProcessSteps {

    private String vertragId;
    private Response letzteAntwort;
    private int letzterTransitionCount;

    @Gegebensei("ein neu angelegter Vertrag")
    public void einNeuAngelegterVertrag() {
        Response r = given()
                .contentType("application/json")
                .body(Map.of(
                        "titel", "BPF-IT",
                        "typ", "SONSTIGES",
                        "erstellerId", UUID.randomUUID().toString()))
                .post("/api/v1/vertraege");
        assertEquals(201, r.statusCode());
        String loc = r.header("Location");
        vertragId = loc.substring(loc.lastIndexOf('/') + 1);
        letzterTransitionCount = 1; // initiale Transition (start)
    }

    @Wenn("die Trigger {string} nacheinander ausgefuehrt werden")
    public void dieTriggerNacheinanderAusgefuehrtWerden(String triggers) {
        String[] sequence = Arrays.stream(triggers.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        for (String t : sequence) {
            letzteAntwort = given()
                    .post("/api/v1/vertraege/" + vertragId + "/process/contract/trigger/" + t);
            if (letzteAntwort.statusCode() == 200) {
                letzterTransitionCount++;
            }
        }
    }

    @Dann("erreicht der Vertrag die Stage {string}")
    public void erreichtDerVertragDieStage(String stage) {
        String aktuell = given().get("/api/v1/vertraege/" + vertragId)
                .then().statusCode(200).extract().jsonPath().getString("stage");
        assertEquals(stage, aktuell);
    }

    @Und("die BPF-Instanz hat {int} Transitions protokolliert")
    public void dieBpfInstanzHatTransitionsProtokolliert(int erwartet) {
        assertEquals(erwartet, letzterTransitionCount,
                "Erwartet " + erwartet + " Transitions, tatsaechlich " + letzterTransitionCount);
    }

    @Wenn("ich den Trigger {string} aufrufe")
    public void ichDenTriggerAufrufe(String trigger) {
        letzteAntwort = given()
                .post("/api/v1/vertraege/" + vertragId + "/process/contract/trigger/" + trigger);
    }

    @Dann("wirft das BPF einen Fehler mit Code {string}")
    public void wirftDasBpfEinenFehlerMitCode(String code) {
        if (letzteAntwort == null || letzteAntwort.statusCode() < 400) {
            fail("Erwartet Fehlerantwort, tatsaechlich Status=" + (letzteAntwort == null ? "null" : letzteAntwort.statusCode()));
        }
        assertEquals(code, letzteAntwort.jsonPath().getString("code"));
    }
}
