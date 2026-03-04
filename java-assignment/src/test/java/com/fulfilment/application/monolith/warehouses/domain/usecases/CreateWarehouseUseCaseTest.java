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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void createShouldPersistWarehouseAndSetTimestamps() {
    Warehouse warehouse = warehouse("MWH.NEW.01", "EINDHOVEN-001", 40, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW.01")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 2, 70));
    when(warehouseStore.getAll()).thenReturn(List.of());

    useCase.create(warehouse);

    assertNotNull(warehouse.createdAt);
    assertNull(warehouse.archivedAt);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  void createShouldFailWhenWarehouseAlreadyExists() {
    Warehouse warehouse = warehouse("MWH.001", "EINDHOVEN-001", 40, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.001"))
        .thenReturn(warehouse("MWH.001", "EINDHOVEN-001", 20, 5));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    verify(locationResolver, never()).resolveByIdentifier(any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void createShouldFailWhenLocationIsInvalid() {
    Warehouse warehouse = warehouse("MWH.NEW.01", "UNKNOWN-001", 40, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW.01")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("UNKNOWN-001")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void createShouldFailWhenStockExceedsCapacity() {
    Warehouse warehouse = warehouse("MWH.NEW.01", "EINDHOVEN-001", 10, 11);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW.01")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 2, 70));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void createShouldFailWhenMaxWarehousesAtLocationReached() {
    Warehouse warehouse = warehouse("MWH.NEW.01", "EINDHOVEN-001", 20, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW.01")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 1, 70));
    when(warehouseStore.getAll())
        .thenReturn(List.of(warehouse("MWH.EXISTING", "EINDHOVEN-001", 20, 5)));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void createShouldFailWhenLocationCapacityWouldBeExceeded() {
    Warehouse warehouse = warehouse("MWH.NEW.01", "EINDHOVEN-001", 25, 10);
    when(warehouseStore.findByBusinessUnitCode("MWH.NEW.01")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("EINDHOVEN-001"))
        .thenReturn(new Location("EINDHOVEN-001", 3, 70));
    when(warehouseStore.getAll())
        .thenReturn(
            List.of(
                warehouse("MWH.OLD.1", "EINDHOVEN-001", 30, 10),
                warehouse("MWH.OLD.2", "EINDHOVEN-001", 20, 10)));

    assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    verify(warehouseStore, never()).create(any());
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
