package ch.grudligstrasse.mda.starter.contract.rest;

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
    void vertrag_erfassen_liefert_201_und_status_entwurf() {
        given().contentType("application/json")
                .body(Map.of(
                        "mandantId", UUID.randomUUID(),
                        "titel", "Rahmenvertrag IT",
                        "vertragsart", "DIENSTLEISTUNG",
                        "partnerId", UUID.randomUUID(),
                        "antragstellerId", UUID.randomUUID()
                ))
                .when().post("/api/v1/vertraege")
                .then()
                .statusCode(201)
                .body("vertragId", notNullValue())
                .body("status", equalTo("ENTWURF"));
    }

    @Test
    void erfassen_ohne_titel_liefert_problem_json() {
        given().contentType("application/json")
                .body(Map.of(
                        "mandantId", UUID.randomUUID(),
                        "vertragsart", "DIENSTLEISTUNG",
                        "partnerId", UUID.randomUUID(),
                        "antragstellerId", UUID.randomUUID()
                ))
                .when().post("/api/v1/vertraege")
                .then()
                .statusCode(400);
    }

    @Test
    void nicht_existierender_vertrag_liefert_404() {
        given()
                .when().get("/api/v1/vertraege/" + UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("code", equalTo("MDA-CON-NOT_FOUND"));
    }
}
