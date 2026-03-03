package com.fulfilment.application.monolith.fulfilmentunits.domain.ports;

public interface ProductLookupPort {

  boolean existsById(Long productId);
}
