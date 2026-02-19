package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreSyncListener {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  void onStoreSyncEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreSyncEvent event) {
    if (event.operation == StoreSyncEvent.Operation.CREATE) {
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
      return;
    }

    legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
  }
}
