package com.fulfilment.application.monolith.fulfilmentunits;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentAssociationEndpointTest {

  private final List<String> warehousesToArchive = new ArrayList<>();

  @AfterEach
  public void cleanupWarehouses() {
    for (String warehouseBusinessUnitCode : warehousesToArchive) {
      int statusCode =
          given().when().delete("/warehouse/" + warehouseBusinessUnitCode).then().extract().statusCode();
      if (statusCode != 204 && statusCode != 404) {
        throw new IllegalStateException(
            "Unexpected status while archiving warehouse "
                + warehouseBusinessUnitCode
                + ": "
                + statusCode);
      }
    }
    warehousesToArchive.clear();
  }

  @Test
  public void testAssociateWarehouseToProductAndStore() {
    Long storeId = createStore();
    Long productId = createProduct();
    String warehouse = createWarehouse("EINDHOVEN-001");

    given()
        .contentType("application/json")
        .body(associationPayload(productId, storeId, warehouse))
        .when()
        .post("/fulfilment-association")
        .then()
        .statusCode(201)
        .body(containsString("\"warehouseBusinessUnitCode\":\"" + warehouse + "\""));
  }

  @Test
  public void testProductCanBeFulfilledByAtMostTwoWarehousesPerStore() {
    Long storeId = createStore();
    Long productId = createProduct();
    String warehouse1 = createWarehouse("AMSTERDAM-001");
    String warehouse2 = createWarehouse("AMSTERDAM-002");
    String warehouse3 = createWarehouse("EINDHOVEN-001");

    associate(productId, storeId, warehouse1).statusCode(201);
    associate(productId, storeId, warehouse2).statusCode(201);
    associate(productId, storeId, warehouse3)
        .statusCode(400)
        .body(containsString("at most 2 warehouses per store"));
  }

  @Test
  public void testStoreCanBeFulfilledByAtMostThreeWarehouses() {
    Long storeId = createStore();
    Long productA = createProduct();
    Long productB = createProduct();
    Long productC = createProduct();
    Long productD = createProduct();
    String warehouse1 = createWarehouse("AMSTERDAM-001");
    String warehouse2 = createWarehouse("AMSTERDAM-002");
    String warehouse3 = createWarehouse("ZWOLLE-002");
    String warehouse4 = createWarehouse("HELMOND-001");

    associate(productA, storeId, warehouse1).statusCode(201);
    associate(productA, storeId, warehouse2).statusCode(201);
    associate(productB, storeId, warehouse3).statusCode(201);
    associate(productC, storeId, warehouse4)
        .statusCode(400)
        .body(containsString("at most 3 warehouses"));

    // Ensure failure is coming from store max warehouse rule, not product-store rule.
    associate(productD, storeId, warehouse1).statusCode(201);
  }

  @Test
  public void testWarehouseCanStoreAtMostFiveProductTypes() {
    Long storeId = createStore();
    String warehouse = createWarehouse("VETSBY-001");
    Long product1 = createProduct();
    Long product2 = createProduct();
    Long product3 = createProduct();
    Long product4 = createProduct();
    Long product5 = createProduct();
    Long product6 = createProduct();

    associate(product1, storeId, warehouse).statusCode(201);
    associate(product2, storeId, warehouse).statusCode(201);
    associate(product3, storeId, warehouse).statusCode(201);
    associate(product4, storeId, warehouse).statusCode(201);
    associate(product5, storeId, warehouse).statusCode(201);
    associate(product6, storeId, warehouse)
        .statusCode(400)
        .body(containsString("at most 5 product types"));
  }

  private io.restassured.response.ValidatableResponse associate(
      Long productId, Long storeId, String warehouseIdentifier) {
    return given()
        .contentType("application/json")
        .body(associationPayload(productId, storeId, warehouseIdentifier))
        .when()
        .post("/fulfilment-association")
        .then();
  }

  private Long createStore() {
    String storeName = "STORE-" + System.nanoTime();
    Number id =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + storeName + "\",\"quantityProductsInStock\":10}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    return id.longValue();
  }

  private Long createProduct() {
    String productName = "PROD-" + System.nanoTime();
    Number id =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + productName + "\",\"stock\":10}")
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    return id.longValue();
  }

  private String createWarehouse(String location) {
    String businessUnitCode = "MWH.TEST." + Long.toHexString(System.nanoTime()).toUpperCase();
    given()
        .contentType("application/json")
        .body(
            "{"
                + "\"businessUnitCode\":\""
                + businessUnitCode
                + "\","
                + "\"location\":\""
                + location
                + "\","
                + "\"capacity\":20,"
                + "\"stock\":5"
                + "}")
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);
    warehousesToArchive.add(businessUnitCode);
    return businessUnitCode;
  }

  private String associationPayload(Long productId, Long storeId, String warehouseIdentifier) {
    return "{"
        + "\"productId\":"
        + productId
        + ","
        + "\"storeId\":"
        + storeId
        + ","
        + "\"warehouseIdentifier\":\""
        + warehouseIdentifier
        + "\""
        + "}";
  }
}
