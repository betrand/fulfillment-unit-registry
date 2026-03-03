package com.fulfilment.application.monolith.fulfilmentunits.domain.ports;

public interface WarehouseLookupPort {

  String resolveBusinessUnitCode(String warehouseIdentifier);
}
