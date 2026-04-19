package ch.grudligstrasse.mda.starter.approval.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class FreigabeResourceTest {

    @Test
    void freigabe_anfordern_liefert_201() {
        given().contentType("application/json")
                .body(Map.of(
                        "vertragId", UUID.randomUUID(),
                        "versionId", UUID.randomUUID(),
                        "reviewerId", UUID.randomUUID()
                ))
                .when().post("/api/v1/freigaben")
                .then()
                .statusCode(201)
                .body("freigabeId", notNullValue())
                .body("entscheidung", equalTo(null));
    }

    @Test
    void entscheiden_ohne_begruendung_bei_ablehnung_liefert_400() {
        String freigabeId = given().contentType("application/json")
                .body(Map.of(
                        "vertragId", UUID.randomUUID(),
                        "versionId", UUID.randomUUID(),
                        "reviewerId", UUID.randomUUID()
                ))
                .when().post("/api/v1/freigaben")
                .then().statusCode(201)
                .extract().jsonPath().getString("freigabeId");

        given().contentType("application/json")
                .body(Map.of("entscheidung", "ABGELEHNT"))
                .when().post("/api/v1/freigaben/" + freigabeId + "/entscheiden")
                .then()
                .statusCode(400)
                .body("code", equalTo("MDA-APV-002"));
    }
}
