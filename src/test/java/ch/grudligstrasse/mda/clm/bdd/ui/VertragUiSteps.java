package ch.grudligstrasse.mda.clm.bdd.ui;

import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Gegebensei;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import io.quarkiverse.cucumber.ScenarioScope;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ScenarioScope
public class VertragUiSteps {

    private Response letzteAntwort;

    @Gegebensei("eine frische Installation")
    public void eineFrischeInstallation() {
        // Nichts zu tun: QuarkusTest liefert ein frisch-migriertes H2.
    }

    @Wenn("ich die Vertragsliste abrufe")
    public void ichDieVertragslisteAbrufe() {
        letzteAntwort = given()
                .queryParam("tenantId", "00000000-0000-0000-0000-000000000099")
                .get("/api/v1/vertraege");
    }

    @Dann("erhalte ich HTTP-Status {int}")
    public void erhalteIchHttpStatus(int status) {
        assertEquals(status, letzteAntwort.statusCode());
    }

    @Und("die Antwort ist eine leere JSON-Liste")
    public void dieAntwortIstEineLeereJsonListe() {
        String body = letzteAntwort.getBody().asString().trim();
        assertTrue(body.equals("[]") || body.equals("[ ]"),
                "Erwartet leere Liste, war: " + body);
    }
}
