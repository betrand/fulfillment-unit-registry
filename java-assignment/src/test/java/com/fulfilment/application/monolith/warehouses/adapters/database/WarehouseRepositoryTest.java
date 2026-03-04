package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseRepositoryTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  @TestTransaction
  void shouldCreateAndFindWarehouseByBusinessUnitCodeAndId() {
    String buCode = "MWH.TEST." + System.nanoTime();
    Warehouse warehouse = warehouse(buCode, "EINDHOVEN-001", 55, 20);
    warehouse.createdAt = LocalDateTime.now();

    warehouseRepository.create(warehouse);

    Warehouse byBuCode = warehouseRepository.findByBusinessUnitCode(buCode);
    assertNotNull(byBuCode);
    assertEquals("EINDHOVEN-001", byBuCode.location);
    assertTrue(warehouseRepository.getAll().stream().anyMatch(w -> buCode.equals(w.businessUnitCode)));

    Long id =
        warehouseRepository.getEntityManager()
            .createQuery(
                "select w.id from DbWarehouse w where w.businessUnitCode = :buCode", Long.class)
            .setParameter("buCode", buCode)
            .getSingleResult();
    assertNotNull(warehouseRepository.findById(id.toString()));
    assertNull(warehouseRepository.findById("not-a-number"));
  }

  @Test
  @TestTransaction
  void shouldUpdateAndRemoveWarehouse() {
    String buCode = "MWH.TEST." + System.nanoTime();
    Warehouse warehouse = warehouse(buCode, "EINDHOVEN-001", 55, 20);
    warehouseRepository.create(warehouse);

    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 60;
    warehouse.stock = 30;
    warehouseRepository.update(warehouse);

    Warehouse updated = warehouseRepository.findByBusinessUnitCode(buCode);
    assertNotNull(updated);
    assertEquals("AMSTERDAM-001", updated.location);
    assertEquals(60, updated.capacity);
    assertEquals(30, updated.stock);

    warehouseRepository.remove(warehouse);
    assertNull(warehouseRepository.findByBusinessUnitCode(buCode));
  }

  @Test
  @TestTransaction
  void updateShouldThrowWhenWarehouseNotFound() {
    Warehouse warehouse = warehouse("MWH.DOES.NOT.EXIST", "EINDHOVEN-001", 10, 5);

    assertThrows(NoSuchElementException.class, () -> warehouseRepository.update(warehouse));
  }

  @Test
  @TestTransaction
  void removeShouldThrowWhenWarehouseNotFound() {
    Warehouse warehouse = warehouse("MWH.DOES.NOT.EXIST", "EINDHOVEN-001", 10, 5);

    assertThrows(NoSuchElementException.class, () -> warehouseRepository.remove(warehouse));
  }

  private Warehouse warehouse(String buCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
