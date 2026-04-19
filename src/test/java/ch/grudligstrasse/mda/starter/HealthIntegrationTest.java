package ch.grudligstrasse.mda.starter;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class HealthIntegrationTest {

    @Test
    void readiness_ist_UP() {
        given().when().get("/q/health/ready")
                .then().statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void liveness_ist_UP() {
        given().when().get("/q/health/live")
                .then().statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void openapi_ist_verfuegbar() {
        given().when().get("/openapi")
                .then().statusCode(200);
    }
}
