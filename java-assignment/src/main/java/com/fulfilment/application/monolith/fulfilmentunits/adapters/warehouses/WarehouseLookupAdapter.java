package com.fulfilment.application.monolith.fulfilmentunits.adapters.warehouses;

import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.WarehouseLookupPort;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseLookupAdapter implements WarehouseLookupPort {

  private final WarehouseRepository warehouseRepository;

  public WarehouseLookupAdapter(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  public String resolveBusinessUnitCode(String warehouseIdentifier) {
    var resolvedWarehouse = warehouseRepository.findByBusinessUnitCode(warehouseIdentifier);
    if (resolvedWarehouse == null) {
      resolvedWarehouse = warehouseRepository.findById(warehouseIdentifier);
    }

    if (resolvedWarehouse == null) {
      return null;
    }

    return resolvedWarehouse.businessUnitCode;
  }
}
