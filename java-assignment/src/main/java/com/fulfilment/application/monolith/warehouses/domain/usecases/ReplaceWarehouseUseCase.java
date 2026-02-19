package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    validateRequiredFields(newWarehouse);

    var currentWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (currentWarehouse == null) {
      throw new NoSuchElementException(
          "Warehouse not found: " + newWarehouse.businessUnitCode);
    }

    var location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid warehouse location: " + newWarehouse.location);
    }

    validateReplacementRules(currentWarehouse, newWarehouse);
    validateCapacityAndStock(newWarehouse, location.maxCapacity);
    validateCapacityAndWarehouseCountAtLocationAfterReplacement(currentWarehouse, newWarehouse, location.maxNumberOfWarehouses, location.maxCapacity);

    currentWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(currentWarehouse);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
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

  private void validateReplacementRules(Warehouse currentWarehouse, Warehouse newWarehouse) {
    if (!Objects.equals(newWarehouse.stock, currentWarehouse.stock)) {
      throw new IllegalArgumentException("Replacement stock must match current warehouse stock");
    }
    if (newWarehouse.capacity < currentWarehouse.stock) {
      throw new IllegalArgumentException(
          "Replacement capacity must accommodate current warehouse stock");
    }
  }

  private void validateCapacityAndWarehouseCountAtLocationAfterReplacement(
      Warehouse currentWarehouse,
      Warehouse replacementWarehouse,
      int locationMaxWarehouses,
      int locationMaxCapacity) {
    var activeWarehousesExcludingCurrent =
        warehouseStore.getAll().stream()
            .filter(warehouse -> !warehouse.businessUnitCode.equals(currentWarehouse.businessUnitCode))
            .toList();

    var warehousesAtReplacementLocation =
        activeWarehousesExcludingCurrent.stream()
            .filter(warehouse -> replacementWarehouse.location.equals(warehouse.location))
            .toList();

    if (warehousesAtReplacementLocation.size() >= locationMaxWarehouses) {
      throw new IllegalArgumentException(
          "Max number of warehouses reached for location: " + replacementWarehouse.location);
    }

    int totalCapacityAtLocation =
        warehousesAtReplacementLocation.stream()
            .map(warehouse -> warehouse.capacity)
            .filter(capacity -> capacity != null)
            .reduce(0, Integer::sum);

    if (totalCapacityAtLocation + replacementWarehouse.capacity > locationMaxCapacity) {
      throw new IllegalArgumentException(
          "Location max capacity exceeded for location: " + replacementWarehouse.location);
    }
  }
}
