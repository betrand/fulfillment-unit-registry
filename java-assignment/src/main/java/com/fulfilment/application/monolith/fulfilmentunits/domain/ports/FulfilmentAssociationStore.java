package com.fulfilment.application.monolith.fulfilmentunits.domain.ports;

import com.fulfilment.application.monolith.fulfilmentunits.FulfilmentAssociation;
import java.util.List;

public interface FulfilmentAssociationStore {

  List<FulfilmentAssociation> listAllById();

  void create(FulfilmentAssociation association);

  boolean existsAssociation(Long productId, Long storeId, String warehouseBusinessUnitCode);

  boolean existsWarehouseForStore(Long storeId, String warehouseBusinessUnitCode);

  boolean existsProductForWarehouse(String warehouseBusinessUnitCode, Long productId);

  long countDistinctWarehousesForProductAndStore(Long productId, Long storeId);

  long countDistinctWarehousesForStore(Long storeId);

  long countDistinctProductsForWarehouse(String warehouseBusinessUnitCode);
}
