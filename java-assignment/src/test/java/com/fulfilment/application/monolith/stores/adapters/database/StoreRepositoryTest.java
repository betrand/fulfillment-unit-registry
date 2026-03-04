package com.fulfilment.application.monolith.stores.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreRepositoryTest {

  @Inject StoreRepository storeRepository;

  @Test
  @TestTransaction
  void shouldCreateFindListAndRemoveStore() {
    Store alpha = new Store("STORE-A-" + System.nanoTime());
    alpha.quantityProductsInStock = 10;
    storeRepository.create(alpha);

    Store beta = new Store("STORE-B-" + System.nanoTime());
    beta.quantityProductsInStock = 5;
    storeRepository.create(beta);

    assertNotNull(alpha.id);
    assertNotNull(beta.id);
    assertNotNull(storeRepository.findById(alpha.id));

    var list = storeRepository.listAllByName();
    assertTrue(list.stream().anyMatch(s -> alpha.name.equals(s.name)));
    assertTrue(list.stream().anyMatch(s -> beta.name.equals(s.name)));

    storeRepository.remove(alpha);
    assertNull(storeRepository.findById(alpha.id));
    assertNotNull(storeRepository.findById(beta.id));
    assertEquals(beta.name, storeRepository.findById(beta.id).name);
  }
}
