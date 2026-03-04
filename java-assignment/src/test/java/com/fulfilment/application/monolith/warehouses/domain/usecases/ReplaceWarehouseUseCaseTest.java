package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void replaceShouldArchiveCurrentAndCreateReplacement() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 40, 10);
    Warehouse replacement = warehouse("MWH.001", "EINDHOVEN-001", 30, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 3, 100));
    when(warehouseStore.getAll()).thenReturn(List.of(current));

    useCase.replace(replacement);

    assertNotNull(current.archivedAt);
    assertNotNull(replacement.createdAt);
    assertNull(replacement.archivedAt);
    verify(warehouseStore).update(current);
    verify(warehouseStore).create(replacement);
  }

  @Test
  void replaceShouldFailWhenCurrentWarehouseNotFound() {
    Warehouse replacement = warehouse("MWH.MISSING", "EINDHOVEN-001", 30, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.MISSING")).thenReturn(null);

    assertThrows(NoSuchElementException.class, () -> useCase.replace(replacement));

    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replaceShouldFailWhenLocationIsInvalid() {
    Warehouse replacement = warehouse("MWH.001", "UNKNOWN-001", 30, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(warehouse("MWH.001", "ZWOLLE-001", 40, 10));
    when(locationResolver.resolveByIdentifier("UNKNOWN-001")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
    verify(warehouseStore, never()).update(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void replaceShouldFailWhenStockDoesNotMatchCurrent() {
    Warehouse replacement = warehouse("MWH.001", "EINDHOVEN-001", 30, 9);
    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(warehouse("MWH.001", "ZWOLLE-001", 40, 10));
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 3, 100));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceShouldFailWhenReplacementCapacityLessThanCurrentStock() {
    Warehouse replacement = warehouse("MWH.001", "EINDHOVEN-001", 9, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(warehouse("MWH.001", "ZWOLLE-001", 40, 10));
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 3, 100));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceShouldFailWhenLocationWarehouseCountWouldBeExceeded() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 40, 10);
    Warehouse replacement = warehouse("MWH.001", "EINDHOVEN-001", 30, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 1, 100));
    when(warehouseStore.getAll())
        .thenReturn(List.of(current, warehouse("MWH.002", "EINDHOVEN-001", 30, 10)));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
  }

  @Test
  void replaceShouldFailWhenLocationCapacityWouldBeExceeded() {
    Warehouse current = warehouse("MWH.001", "ZWOLLE-001", 40, 10);
    Warehouse replacement = warehouse("MWH.001", "EINDHOVEN-001", 30, 10);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 3, 50));
    when(warehouseStore.getAll())
        .thenReturn(List.of(current, warehouse("MWH.002", "EINDHOVEN-001", 25, 10)));

    assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
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
