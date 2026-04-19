package ch.grudligstrasse.mda.starter.obligation.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class FristResourceTest {

    @Test
    void frist_erfassen_liefert_201_und_status_offen() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vertragId", UUID.randomUUID());
        body.put("art", "KUENDIGUNG");
        body.put("faelligkeitsDatum", "2026-12-31");
        body.put("vorlaufTage", 30);
        body.put("verantwortlicherId", UUID.randomUUID());

        given().contentType("application/json")
                .body(body)
                .when().post("/api/v1/fristen")
                .then()
                .statusCode(201)
                .body("status", equalTo("OFFEN"))
                .body("erinnerungsDatum", equalTo("2026-12-01"));
    }

    @Test
    void sichten_einer_offenen_frist_wird_abgelehnt() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vertragId", UUID.randomUUID());
        body.put("art", "ABLAUF");
        body.put("faelligkeitsDatum", "2026-12-31");
        body.put("vorlaufTage", 7);
        body.put("verantwortlicherId", UUID.randomUUID());

        String fristId = given().contentType("application/json")
                .body(body)
                .when().post("/api/v1/fristen")
                .then().statusCode(201)
                .extract().jsonPath().getString("fristId");

        given().contentType("application/json")
                .body(Map.of("sichtenderUserId", UUID.randomUUID()))
                .when().post("/api/v1/fristen/" + fristId + "/sichten")
                .then()
                .statusCode(400)
                .body("code", equalTo("MDA-OBL-011"));
    }
}
