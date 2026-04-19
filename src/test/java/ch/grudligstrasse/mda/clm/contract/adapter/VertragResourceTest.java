package ch.grudligstrasse.mda.clm.contract.adapter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class VertragResourceTest {

    @Test
    void postErzeugtVertragMitLocationHeader() {
        String location = given()
                .contentType("application/json")
                .body(Map.of(
                        "titel", "Testvertrag",
                        "typ", "KUNDENVERTRAG",
                        "erstellerId", UUID.randomUUID().toString()))
                .when().post("/api/v1/vertraege")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("id", notNullValue())
                .extract().header("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);
        given()
                .when().get("/api/v1/vertraege/" + id)
                .then()
                .statusCode(200)
                .body("titel", equalTo("Testvertrag"))
                .body("stage", equalTo("ENTWURF"));
    }

    @Test
    void triggerEinreichen_aendertStage() {
        String location = given()
                .contentType("application/json")
                .body(Map.of(
                        "titel", "Einreichen-Test",
                        "typ", "SONSTIGES",
                        "erstellerId", UUID.randomUUID().toString()))
                .when().post("/api/v1/vertraege")
                .then().statusCode(201).extract().header("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);

        given()
                .contentType("application/json")
                .when().post("/api/v1/vertraege/" + id + "/process/contract/trigger/einreichen")
                .then()
                .statusCode(200)
                .body("stage", equalTo("IN_PRUEFUNG"));
    }

    @Test
    void ungueltigerTrigger_liefertProblemDetail() {
        String location = given()
                .contentType("application/json")
                .body(Map.of(
                        "titel", "Invalid-Trigger",
                        "typ", "SONSTIGES",
                        "erstellerId", UUID.randomUUID().toString()))
                .when().post("/api/v1/vertraege")
                .then().statusCode(201).extract().header("Location");
        String id = location.substring(location.lastIndexOf('/') + 1);

        given()
                .contentType("application/json")
                .when().post("/api/v1/vertraege/" + id + "/process/contract/trigger/unterzeichnen")
                .then()
                .statusCode(422)
                .body("code", equalTo("MDA-BPF-001"));
    }

    @Test
    void notFoundVertrag_liefert404() {
        given()
                .when().get("/api/v1/vertraege/" + UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("code", equalTo("MDA-CON-404"));
    }
}
