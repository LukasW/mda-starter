package ch.grudligstrasse.mda.clm.person.adapter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
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
                .body("[0].email", equalTo("suchbar@test.ch"))
                .body("[0].versionNumber", equalTo(0));
    }

    @Test
    void putAendertInternePerson() {
        String id = erfassen("Putt", "Eins", "putt.eins@test.ch");

        given()
                .contentType("application/json")
                .body(Map.of(
                        "vorname", "Putt",
                        "nachname", "Geandert",
                        "email", "putt.eins@test.ch",
                        "expectedVersion", 0))
                .when().put("/api/v1/personen/" + id)
                .then().statusCode(204);

        given().when().get("/api/v1/personen/" + id)
                .then().statusCode(200)
                .body("nachname", equalTo("Geandert"))
                .body("versionNumber", equalTo(1));
    }

    @Test
    void putMitFalscherVersionGibt409() {
        String id = erfassen("Konflikt", "Test", "konflikt@test.ch");

        given()
                .contentType("application/json")
                .body(Map.of(
                        "vorname", "Konflikt",
                        "nachname", "Test",
                        "email", "konflikt@test.ch",
                        "expectedVersion", 99))
                .when().put("/api/v1/personen/" + id)
                .then().statusCode(409)
                .body("code", equalTo("MDA-PER-409"));
    }

    @Test
    void deleteEntferntInternePerson() {
        String id = erfassen("Loesch", "Mich", "loesch.mich@test.ch");

        given().when().delete("/api/v1/personen/" + id + "?expectedVersion=0")
                .then().statusCode(204);

        given().when().get("/api/v1/personen/" + id)
                .then().statusCode(404)
                .body("code", equalTo("MDA-PER-404"));
    }

    @Test
    void deleteAufBereitsGeloeschterIdGibt404() {
        String id = erfassen("Twice", "Deleted", "twice.deleted@test.ch");
        given().when().delete("/api/v1/personen/" + id + "?expectedVersion=0").then().statusCode(204);

        given().when().delete("/api/v1/personen/" + id + "?expectedVersion=1")
                .then().statusCode(404)
                .body("code", equalTo("MDA-PER-404"));
    }

    @Test
    void putAufNichtExistenterIdGibt404() {
        Map<String, Object> body = new HashMap<>();
        body.put("vorname", "X");
        body.put("nachname", "Y");
        body.put("email", "x@y.ch");
        body.put("expectedVersion", 0);

        given()
                .contentType("application/json")
                .body(body)
                .when().put("/api/v1/personen/00000000-0000-0000-0000-000000000999")
                .then().statusCode(404)
                .body("code", equalTo("MDA-PER-404"));
    }

    private String erfassen(String vorname, String nachname, String email) {
        return given()
                .contentType("application/json")
                .body(Map.of("vorname", vorname, "nachname", nachname, "email", email))
                .when().post("/api/v1/personen")
                .then().statusCode(201)
                .extract().jsonPath().getString("id");
    }
}
