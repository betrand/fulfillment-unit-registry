package com.fulfilment.application.monolith.stores.adapters.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreSyncEvent;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CdiStoreSyncPublisherTest {

  private Event<StoreSyncEvent> storeSyncEvents;
  private CdiStoreSyncPublisher publisher;

  @BeforeEach
  void setUp() {
    storeSyncEvents = mock(Event.class);
    publisher = new CdiStoreSyncPublisher();
    publisher.storeSyncEvents = storeSyncEvents;
  }

  @Test
  void publishCreatedShouldFireCreateEventWithSnapshot() {
    Store store = new Store("TONSTAD");
    store.id = 1L;
    store.quantityProductsInStock = 10;

    publisher.publishCreated(store);

    ArgumentCaptor<StoreSyncEvent> eventCaptor = ArgumentCaptor.forClass(StoreSyncEvent.class);
    verify(storeSyncEvents).fire(eventCaptor.capture());
    StoreSyncEvent event = eventCaptor.getValue();
    assertEquals(StoreSyncEvent.Operation.CREATE, event.operation);
    assertNotSame(store, event.store);
    assertEquals(store.id, event.store.id);
    assertEquals(store.name, event.store.name);
    assertEquals(store.quantityProductsInStock, event.store.quantityProductsInStock);
  }

  @Test
  void publishUpdatedShouldFireUpdateEventWithSnapshot() {
    Store store = new Store("KALLAX");
    store.id = 2L;
    store.quantityProductsInStock = 7;

    publisher.publishUpdated(store);

    ArgumentCaptor<StoreSyncEvent> eventCaptor = ArgumentCaptor.forClass(StoreSyncEvent.class);
    verify(storeSyncEvents).fire(eventCaptor.capture());
    StoreSyncEvent event = eventCaptor.getValue();
    assertEquals(StoreSyncEvent.Operation.UPDATE, event.operation);
    assertNotSame(store, event.store);
    assertEquals(store.id, event.store.id);
    assertEquals(store.name, event.store.name);
    assertEquals(store.quantityProductsInStock, event.store.quantityProductsInStock);
  }
}
