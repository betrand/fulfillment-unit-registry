package com.fulfilment.application.monolith.stores.adapters.events;

import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreSyncEvent;
import com.fulfilment.application.monolith.stores.domain.ports.StoreSyncPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class CdiStoreSyncPublisher implements StoreSyncPublisher {

  @Inject Event<StoreSyncEvent> storeSyncEvents;

  @Override
  public void publishCreated(Store store) {
    storeSyncEvents.fire(StoreSyncEvent.created(snapshot(store)));
  }

  @Override
  public void publishUpdated(Store store) {
    storeSyncEvents.fire(StoreSyncEvent.updated(snapshot(store)));
  }

  private Store snapshot(Store entity) {
    Store snapshot = new Store();
    snapshot.id = entity.id;
    snapshot.name = entity.name;
    snapshot.quantityProductsInStock = entity.quantityProductsInStock;
    return snapshot;
  }
}
