package ch.grudligstrasse.mda.clm.person.adapter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Bean-Validation-Tests fuer {@code /api/v1/personen}-Endpunkte.
 */
@QuarkusTest
class PersonResourceValidationTest {

    @Test
    void postOhneVornameGibt400() {
        Map<String, Object> body = new HashMap<>();
        body.put("vorname", "");
        body.put("nachname", "Muster");
        body.put("email", "max@muster.ch");

        given()
                .contentType("application/json")
                .body(body)
                .when().post("/api/v1/personen")
                .then().statusCode(400);
    }

    @Test
    void postMitUngueltigerEmailGibt400() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "vorname", "Max",
                        "nachname", "Muster",
                        "email", "kein-email"))
                .when().post("/api/v1/personen")
                .then().statusCode(400);
    }

    @Test
    void putOhneNachnameGibt400() {
        String id = given()
                .contentType("application/json")
                .body(Map.of("vorname", "Val", "nachname", "Idierung", "email", "val@id.ch"))
                .when().post("/api/v1/personen")
                .then().statusCode(201)
                .extract().jsonPath().getString("id");

        Map<String, Object> body = new HashMap<>();
        body.put("vorname", "Val");
        body.put("nachname", "");
        body.put("email", "val@id.ch");
        body.put("expectedVersion", 0);

        given()
                .contentType("application/json")
                .body(body)
                .when().put("/api/v1/personen/" + id)
                .then().statusCode(400);
    }
}
