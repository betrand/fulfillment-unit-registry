package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LegacyStoreManagerGatewayTest {

  @Test
  void createStoreOnLegacySystemShouldCompleteWithoutException() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("TONSTAD");
    store.quantityProductsInStock = 10;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  void updateStoreOnLegacySystemShouldCompleteWithoutException() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("KALLAX");
    store.quantityProductsInStock = 5;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
