package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.stores.application.usecases.StoreUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoreResourceUnitTest {

  private StoreUseCase storeUseCase;
  private StoreResource resource;

  @BeforeEach
  void setUp() {
    storeUseCase = mock(StoreUseCase.class);
    resource = new StoreResource();
    resource.storeUseCase = storeUseCase;
  }

  @Test
  void getShouldDelegateToUseCase() {
    List<Store> stores = List.of(new Store("TONSTAD"));
    when(storeUseCase.list()).thenReturn(stores);

    assertSame(stores, resource.get());
  }

  @Test
  void getSingleShouldDelegateToUseCase() {
    Store store = new Store("KALLAX");
    when(storeUseCase.getSingle(1L)).thenReturn(store);

    assertSame(store, resource.getSingle(1L));
  }

  @Test
  void createShouldReturnCreatedStatusAndDelegateToUseCase() {
    Store store = new Store("BESTA");

    var response = resource.create(store);

    assertEquals(201, response.getStatus());
    verify(storeUseCase).create(store);
  }

  @Test
  void updateShouldDelegateToUseCase() {
    Store incoming = new Store("NEW");
    Store updated = new Store("UPDATED");
    when(storeUseCase.update(5L, incoming)).thenReturn(updated);

    assertSame(updated, resource.update(5L, incoming));
  }

  @Test
  void patchShouldDelegateToUseCase() {
    Store incoming = new Store("NEW");
    Store patched = new Store("PATCHED");
    when(storeUseCase.patch(5L, incoming)).thenReturn(patched);

    assertSame(patched, resource.patch(5L, incoming));
  }

  @Test
  void deleteShouldReturnNoContentAndDelegateToUseCase() {
    var response = resource.delete(7L);

    assertEquals(204, response.getStatus());
    verify(storeUseCase).delete(7L);
  }
}
