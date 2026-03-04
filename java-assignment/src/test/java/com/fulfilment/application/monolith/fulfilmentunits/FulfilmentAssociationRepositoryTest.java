package com.fulfilment.application.monolith.fulfilmentunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class FulfilmentAssociationRepositoryTest {

  @Inject FulfilmentAssociationRepository fulfilmentAssociationRepository;

  @Test
  @TestTransaction
  void shouldPersistAndEvaluateAssociationQueries() {
    String warehouseCodeOne = "TEST-WH-1-" + System.nanoTime();
    String warehouseCodeTwo = "TEST-WH-2-" + System.nanoTime();

    fulfilmentAssociationRepository.create(association(1L, 1L, warehouseCodeOne));
    fulfilmentAssociationRepository.create(association(1L, 1L, warehouseCodeTwo));
    fulfilmentAssociationRepository.create(association(2L, 1L, warehouseCodeTwo));

    assertFalse(fulfilmentAssociationRepository.listAllById().isEmpty());
    assertTrue(fulfilmentAssociationRepository.existsAssociation(1L, 1L, warehouseCodeOne));
    assertFalse(fulfilmentAssociationRepository.existsAssociation(1L, 2L, warehouseCodeOne));
    assertTrue(fulfilmentAssociationRepository.existsWarehouseForStore(1L, warehouseCodeTwo));
    assertFalse(fulfilmentAssociationRepository.existsWarehouseForStore(2L, warehouseCodeTwo));
    assertTrue(fulfilmentAssociationRepository.existsProductForWarehouse(warehouseCodeTwo, 2L));
    assertFalse(fulfilmentAssociationRepository.existsProductForWarehouse(warehouseCodeOne, 2L));
    assertEquals(
        2L,
        fulfilmentAssociationRepository.countDistinctWarehousesForProductAndStore(1L, 1L));
    assertEquals(2L, fulfilmentAssociationRepository.countDistinctWarehousesForStore(1L));
    assertEquals(2L, fulfilmentAssociationRepository.countDistinctProductsForWarehouse(warehouseCodeTwo));
  }

  private FulfilmentAssociation association(Long productId, Long storeId, String warehouseCode) {
    FulfilmentAssociation association = new FulfilmentAssociation();
    association.productId = productId;
    association.storeId = storeId;
    association.warehouseBusinessUnitCode = warehouseCode;
    return association;
  }
}
