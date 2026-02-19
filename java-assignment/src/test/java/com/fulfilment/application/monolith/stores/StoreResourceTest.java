package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTest {

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @BeforeEach
  void setUp() {
    reset(legacyStoreManagerGateway);
  }

  @Test
  public void testCreateStoreShouldSyncWithLegacyAfterCommit() {
    String storeName = "STORE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    given()
        .contentType("application/json")
        .body("{\"name\":\"" + storeName + "\",\"quantityProductsInStock\":15}")
        .when()
        .post("/store")
        .then()
        .statusCode(201);

    verify(legacyStoreManagerGateway, times(1))
        .createStoreOnLegacySystem(
            argThat(
                store ->
                    store != null
                        && storeName.equals(store.name)
                        && store.quantityProductsInStock == 15));
    verifyNoMoreInteractions(legacyStoreManagerGateway);
  }

  @Test
  public void testCreateStoreShouldNotSyncWithLegacyWhenTransactionFails() {
    given()
        .contentType("application/json")
        .body("{\"name\":\"TONSTAD\",\"quantityProductsInStock\":99}")
        .when()
        .post("/store")
        .then()
        .statusCode(500);

    verifyNoInteractions(legacyStoreManagerGateway);
  }

  @Test
  public void testUpdateStoreShouldSyncWithLegacyAfterCommit() {
    given()
        .contentType("application/json")
        .body("{\"name\":\"KALLAX\",\"quantityProductsInStock\":7}")
        .when()
        .put("/store/2")
        .then()
        .statusCode(200);

    verify(legacyStoreManagerGateway, times(1))
        .updateStoreOnLegacySystem(
            argThat(
                store ->
                    store != null
                        && "KALLAX".equals(store.name)
                        && store.quantityProductsInStock == 7));
    verifyNoMoreInteractions(legacyStoreManagerGateway);
  }
}
