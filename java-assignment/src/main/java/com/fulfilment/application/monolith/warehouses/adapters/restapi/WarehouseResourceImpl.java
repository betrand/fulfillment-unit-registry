package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.NoSuchElementException;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseUseCase createWarehouseUseCase;
  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = toDomainWarehouse(data);
    try {
      createWarehouseUseCase.create(warehouse);
      return toWarehouseResponse(warehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = findWarehouseByIdentifier(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }

    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  @ResponseStatus(204)
  public void archiveAWarehouseUnitByID(String id) {
    var warehouse = findWarehouseByIdentifier(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }

    try {
      archiveWarehouseUseCase.archive(warehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    var warehouse = toDomainWarehouse(data);
    warehouse.businessUnitCode = businessUnitCode;
    try {
      replaceWarehouseUseCase.replace(warehouse);
      return toWarehouseResponse(warehouseRepository.findByBusinessUnitCode(businessUnitCode));
    } catch (NoSuchElementException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    response.setId(warehouse.businessUnitCode);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(
      Warehouse request) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = request.getBusinessUnitCode();
    warehouse.location = request.getLocation();
    warehouse.capacity = request.getCapacity();
    warehouse.stock = request.getStock();
    return warehouse;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse findWarehouseByIdentifier(
      String identifier) {
    var warehouse = warehouseRepository.findByBusinessUnitCode(identifier);
    if (warehouse != null) {
      return warehouse;
    }

    return warehouseRepository.findById(identifier);
  }
}
