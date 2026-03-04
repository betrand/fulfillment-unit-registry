package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DbWarehouseTest {

  @Test
  void toWarehouseShouldMapAllFields() {
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = "MWH.100";
    entity.location = "ZWOLLE-001";
    entity.capacity = 80;
    entity.stock = 10;
    entity.createdAt = LocalDateTime.now().minusDays(1);
    entity.archivedAt = LocalDateTime.now();

    var model = entity.toWarehouse();

    assertEquals("MWH.100", model.businessUnitCode);
    assertEquals("ZWOLLE-001", model.location);
    assertEquals(80, model.capacity);
    assertEquals(10, model.stock);
    assertSame(entity.createdAt, model.createdAt);
    assertSame(entity.archivedAt, model.archivedAt);
  }
}
