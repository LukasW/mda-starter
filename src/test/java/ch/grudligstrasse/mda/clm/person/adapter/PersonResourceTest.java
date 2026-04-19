package ch.grudligstrasse.mda.clm.person.adapter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class PersonResourceTest {

    @Test
    void postErfasstPerson() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "vorname", "Max",
                        "nachname", "Muster",
                        "email", "max@muster.ch"))
                .when().post("/api/v1/personen")
                .then()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    void getSuchenLiefertListe() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "vorname", "Suchbar",
                        "nachname", "Test",
                        "email", "suchbar@test.ch"))
                .when().post("/api/v1/personen")
                .then().statusCode(201);

        given()
                .when().get("/api/v1/personen?query=suchbar")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].email", equalTo("suchbar@test.ch"));
    }
}
