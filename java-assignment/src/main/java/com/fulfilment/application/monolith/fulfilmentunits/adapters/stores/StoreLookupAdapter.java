package com.fulfilment.application.monolith.fulfilmentunits.adapters.stores;

import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.StoreLookupPort;
import com.fulfilment.application.monolith.stores.adapters.database.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreLookupAdapter implements StoreLookupPort {

  private final StoreRepository storeRepository;

  public StoreLookupAdapter(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Override
  public boolean existsById(Long storeId) {
    return storeRepository.findById(storeId) != null;
  }
}
