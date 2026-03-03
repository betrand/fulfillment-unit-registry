package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.Store;

public interface StoreSyncPublisher {

  void publishCreated(Store store);

  void publishUpdated(Store store);
}
