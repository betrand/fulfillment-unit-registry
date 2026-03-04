package com.fulfilment.application.monolith.fulfilmentunits.adapters.warehouses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

class WarehouseLookupAdapterTest {

  @Test
  void resolveBusinessUnitCodeShouldUseBuCodeLookupFirst() {
    WarehouseRepository repository = mock(WarehouseRepository.class);
    Warehouse warehouse = warehouse("MWH.001");
    when(repository.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);
    WarehouseLookupAdapter adapter = new WarehouseLookupAdapter(repository);

    assertEquals("MWH.001", adapter.resolveBusinessUnitCode("MWH.001"));
  }

  @Test
  void resolveBusinessUnitCodeShouldFallbackToIdLookup() {
    WarehouseRepository repository = mock(WarehouseRepository.class);
    Warehouse warehouse = warehouse("MWH.002");
    when(repository.findByBusinessUnitCode("2")).thenReturn(null);
    when(repository.findById("2")).thenReturn(warehouse);
    WarehouseLookupAdapter adapter = new WarehouseLookupAdapter(repository);

    assertEquals("MWH.002", adapter.resolveBusinessUnitCode("2"));
  }

  @Test
  void resolveBusinessUnitCodeShouldReturnNullWhenNotFound() {
    WarehouseRepository repository = mock(WarehouseRepository.class);
    when(repository.findByBusinessUnitCode("MISSING")).thenReturn(null);
    when(repository.findById("MISSING")).thenReturn(null);
    WarehouseLookupAdapter adapter = new WarehouseLookupAdapter(repository);

    assertNull(adapter.resolveBusinessUnitCode("MISSING"));
  }

  private Warehouse warehouse(String buCode) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    return warehouse;
  }
}
