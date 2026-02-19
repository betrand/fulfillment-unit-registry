package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointTest {

  @Test
  public void testCreateGetAndArchiveWarehouseFlow() {
    String businessUnitCode = randomBusinessUnitCode();

    given()
        .contentType("application/json")
        .body(
            warehousePayload(businessUnitCode, "AMSTERDAM-002", 20, 10))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString(businessUnitCode), containsString("AMSTERDAM-002"));

    given()
        .when()
        .get("/warehouse/" + businessUnitCode)
        .then()
        .statusCode(200)
        .body(containsString(businessUnitCode), containsString("AMSTERDAM-002"));

    given().when().delete("/warehouse/" + businessUnitCode).then().statusCode(204);

    given().when().get("/warehouse/" + businessUnitCode).then().statusCode(404);
  }

  @Test
  public void testReplaceWarehouseFlow() {
    String businessUnitCode = randomBusinessUnitCode();

    given()
        .contentType("application/json")
        .body(
            warehousePayload(businessUnitCode, "AMSTERDAM-001", 15, 7))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body(warehousePayload("IGNORED", "AMSTERDAM-001", 20, 7))
        .when()
        .post("/warehouse/" + businessUnitCode + "/replacement")
        .then()
        .statusCode(200)
        .body(containsString(businessUnitCode), containsString("AMSTERDAM-001"));

    given()
        .when()
        .get("/warehouse/" + businessUnitCode)
        .then()
        .statusCode(200)
        .body(containsString(businessUnitCode), containsString("\"capacity\":20"));
  }

  @Test
  public void testCreateWarehouseWithInvalidLocationShouldReturnBadRequest() {
    String businessUnitCode = randomBusinessUnitCode();

    given()
        .contentType("application/json")
        .body(warehousePayload(businessUnitCode, "UNKNOWN-001", 20, 10))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  public void testCreateWarehouseWithExistingBusinessUnitCodeShouldReturnBadRequest() {
    given()
        .contentType("application/json")
        .body(warehousePayload("MWH.001", "AMSTERDAM-001", 20, 10))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  public void testReplaceWarehouseWithStockMismatchShouldReturnBadRequest() {
    String businessUnitCode = randomBusinessUnitCode();

    given()
        .contentType("application/json")
        .body(warehousePayload(businessUnitCode, "AMSTERDAM-002", 20, 8))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body(warehousePayload("IGNORED", "AMSTERDAM-002", 25, 7))
        .when()
        .post("/warehouse/" + businessUnitCode + "/replacement")
        .then()
        .statusCode(400);
  }

  private String randomBusinessUnitCode() {
    return "MWH.NEW." + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private String warehousePayload(String businessUnitCode, String location, int capacity, int stock) {
    return "{"
        + "\"businessUnitCode\":\""
        + businessUnitCode
        + "\","
        + "\"location\":\""
        + location
        + "\","
        + "\"capacity\":"
        + capacity
        + ","
        + "\"stock\":"
        + stock
        + "}";
  }
}
