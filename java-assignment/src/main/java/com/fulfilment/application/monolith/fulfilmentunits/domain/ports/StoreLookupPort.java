package com.fulfilment.application.monolith.fulfilmentunits.domain.ports;

public interface StoreLookupPort {

  boolean existsById(Long storeId);
}
