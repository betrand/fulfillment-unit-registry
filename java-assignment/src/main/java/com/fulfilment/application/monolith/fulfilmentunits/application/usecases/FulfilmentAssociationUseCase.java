package com.fulfilment.application.monolith.fulfilmentunits.application.usecases;

import com.fulfilment.application.monolith.fulfilmentunits.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilmentunits.FulfilmentAssociationRequest;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.FulfilmentAssociationStore;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.ProductLookupPort;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.StoreLookupPort;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.WarehouseLookupPort;
import com.fulfilment.application.monolith.shared.application.exceptions.BadRequestException;
import com.fulfilment.application.monolith.shared.application.exceptions.ConflictException;
import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssociationUseCase {

  private final ProductLookupPort productLookupPort;
  private final StoreLookupPort storeLookupPort;
  private final WarehouseLookupPort warehouseLookupPort;
  private final FulfilmentAssociationStore fulfilmentAssociationStore;

  public FulfilmentAssociationUseCase(
      ProductLookupPort productLookupPort,
      StoreLookupPort storeLookupPort,
      WarehouseLookupPort warehouseLookupPort,
      FulfilmentAssociationStore fulfilmentAssociationStore) {
    this.productLookupPort = productLookupPort;
    this.storeLookupPort = storeLookupPort;
    this.warehouseLookupPort = warehouseLookupPort;
    this.fulfilmentAssociationStore = fulfilmentAssociationStore;
  }

  public List<FulfilmentAssociation> list() {
    return fulfilmentAssociationStore.listAllById();
  }

  @Transactional
  public FulfilmentAssociation associate(FulfilmentAssociationRequest request) {
    validateRequest(request);

    if (!productLookupPort.existsById(request.productId)) {
      throw new NotFoundException("Product with id of " + request.productId + " does not exist.");
    }

    if (!storeLookupPort.existsById(request.storeId)) {
      throw new NotFoundException("Store with id of " + request.storeId + " does not exist.");
    }

    String warehouseBusinessUnitCode =
        warehouseLookupPort.resolveBusinessUnitCode(request.warehouseIdentifier);
    if (warehouseBusinessUnitCode == null) {
      throw new NotFoundException(
          "Warehouse with identifier " + request.warehouseIdentifier + " does not exist.");
    }

    if (fulfilmentAssociationStore.existsAssociation(
        request.productId, request.storeId, warehouseBusinessUnitCode)) {
      throw new ConflictException("Association already exists for product/store/warehouse combination.");
    }

    long warehousesForProductAndStore =
        fulfilmentAssociationStore.countDistinctWarehousesForProductAndStore(
            request.productId, request.storeId);
    if (warehousesForProductAndStore >= 2) {
      throw new BadRequestException("A product can be fulfilled by at most 2 warehouses per store.");
    }

    if (!fulfilmentAssociationStore.existsWarehouseForStore(
        request.storeId, warehouseBusinessUnitCode)) {
      long warehousesForStore =
          fulfilmentAssociationStore.countDistinctWarehousesForStore(request.storeId);
      if (warehousesForStore >= 3) {
        throw new BadRequestException("A store can be fulfilled by at most 3 warehouses.");
      }
    }

    if (!fulfilmentAssociationStore.existsProductForWarehouse(
        warehouseBusinessUnitCode, request.productId)) {
      long productsForWarehouse =
          fulfilmentAssociationStore.countDistinctProductsForWarehouse(warehouseBusinessUnitCode);
      if (productsForWarehouse >= 5) {
        throw new BadRequestException("A warehouse can store at most 5 product types.");
      }
    }

    FulfilmentAssociation association = new FulfilmentAssociation();
    association.productId = request.productId;
    association.storeId = request.storeId;
    association.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    fulfilmentAssociationStore.create(association);

    return association;
  }

  private void validateRequest(FulfilmentAssociationRequest request) {
    if (request == null) {
      throw new UnprocessableEntityException("Request body is required.");
    }
    if (request.productId == null) {
      throw new UnprocessableEntityException("productId is required.");
    }
    if (request.storeId == null) {
      throw new UnprocessableEntityException("storeId is required.");
    }
    if (request.warehouseIdentifier == null || request.warehouseIdentifier.isBlank()) {
      throw new UnprocessableEntityException("warehouseIdentifier is required.");
    }
  }
}
