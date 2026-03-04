package com.fulfilment.application.monolith.stores.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreStore;
import com.fulfilment.application.monolith.stores.domain.ports.StoreSyncPublisher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreUseCaseTest {

  private StoreStore storeStore;
  private StoreSyncPublisher storeSyncPublisher;
  private StoreUseCase useCase;

  @BeforeEach
  void setUp() {
    storeStore = mock(StoreStore.class);
    storeSyncPublisher = mock(StoreSyncPublisher.class);
    useCase = new StoreUseCase(storeStore, storeSyncPublisher);
  }

  @Test
  void listShouldDelegateToStore() {
    List<Store> stores = List.of(new Store("S1"));
    when(storeStore.listAllByName()).thenReturn(stores);

    assertSame(stores, useCase.list());
  }

  @Test
  void getSingleShouldFailWhenStoreNotFound() {
    when(storeStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.getSingle(1L));
  }

  @Test
  void createShouldFailWhenIdIsProvided() {
    Store store = new Store("S1");
    store.id = 1L;

    assertThrows(UnprocessableEntityException.class, () -> useCase.create(store));
  }

  @Test
  void createShouldPersistAndPublishCreatedEvent() {
    Store store = new Store("S1");

    Store created = useCase.create(store);

    assertSame(store, created);
    verify(storeStore).create(store);
    verify(storeSyncPublisher).publishCreated(store);
  }

  @Test
  void updateShouldFailWhenNameMissing() {
    Store incoming = new Store();

    assertThrows(UnprocessableEntityException.class, () -> useCase.update(1L, incoming));
  }

  @Test
  void updateShouldFailWhenStoreNotFound() {
    Store incoming = new Store("S1");
    when(storeStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.update(1L, incoming));
  }

  @Test
  void updateShouldMutateStoreAndPublishUpdatedEvent() {
    Store existing = new Store("OLD");
    existing.quantityProductsInStock = 5;
    Store incoming = new Store("NEW");
    incoming.quantityProductsInStock = 10;
    when(storeStore.findById(1L)).thenReturn(existing);

    Store updated = useCase.update(1L, incoming);

    assertSame(existing, updated);
    assertEquals("NEW", existing.name);
    assertEquals(10, existing.quantityProductsInStock);
    verify(storeSyncPublisher).publishUpdated(existing);
  }

  @Test
  void patchShouldMutateFieldsWhenCurrentValuesAllowIt() {
    Store existing = new Store("OLD");
    existing.quantityProductsInStock = 7;
    Store incoming = new Store("NEW");
    incoming.quantityProductsInStock = 9;
    when(storeStore.findById(1L)).thenReturn(existing);

    Store patched = useCase.patch(1L, incoming);

    assertSame(existing, patched);
    assertEquals("NEW", existing.name);
    assertEquals(9, existing.quantityProductsInStock);
    verify(storeSyncPublisher).publishUpdated(existing);
  }

  @Test
  void patchShouldKeepFieldsWhenCurrentValuesBlockMutation() {
    Store existing = new Store();
    existing.name = null;
    existing.quantityProductsInStock = 0;
    Store incoming = new Store("NEW");
    incoming.quantityProductsInStock = 99;
    when(storeStore.findById(1L)).thenReturn(existing);

    Store patched = useCase.patch(1L, incoming);

    assertSame(existing, patched);
    assertNull(existing.name);
    assertEquals(0, existing.quantityProductsInStock);
    verify(storeSyncPublisher).publishUpdated(existing);
  }

  @Test
  void patchShouldFailWhenNameMissing() {
    Store incoming = new Store();

    assertThrows(UnprocessableEntityException.class, () -> useCase.patch(1L, incoming));
  }

  @Test
  void patchShouldFailWhenStoreNotFound() {
    Store incoming = new Store("S1");
    when(storeStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.patch(1L, incoming));
  }

  @Test
  void deleteShouldFailWhenStoreNotFound() {
    when(storeStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.delete(1L));
  }

  @Test
  void deleteShouldRemoveStoreWhenFound() {
    Store existing = new Store("S1");
    when(storeStore.findById(1L)).thenReturn(existing);

    useCase.delete(1L);

    verify(storeStore).remove(existing);
  }
}
