package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    validateRequiredFields(warehouse);

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code already exists: " + warehouse.businessUnitCode);
    }

    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid warehouse location: " + warehouse.location);
    }

    validateCapacityAndStock(warehouse, location.maxCapacity);

    var warehousesAtLocation =
        warehouseStore.getAll().stream().filter(w -> warehouse.location.equals(w.location)).toList();

    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Max number of warehouses reached for location: " + warehouse.location);
    }

    int totalCapacityAtLocation =
        warehousesAtLocation.stream()
            .map(w -> w.capacity)
            .filter(capacity -> capacity != null)
            .reduce(0, Integer::sum);

    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Location max capacity exceeded for location: " + warehouse.location);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }

  private void validateRequiredFields(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse payload is required");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new IllegalArgumentException("Location is required");
    }
    if (warehouse.capacity == null) {
      throw new IllegalArgumentException("Capacity is required");
    }
    if (warehouse.stock == null) {
      throw new IllegalArgumentException("Stock is required");
    }
  }

  private void validateCapacityAndStock(Warehouse warehouse, int locationMaxCapacity) {
    if (warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than zero");
    }
    if (warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock must be greater or equal to zero");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity");
    }
    if (warehouse.capacity > locationMaxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity exceeds max capacity for location: " + warehouse.location);
    }
  }
}
