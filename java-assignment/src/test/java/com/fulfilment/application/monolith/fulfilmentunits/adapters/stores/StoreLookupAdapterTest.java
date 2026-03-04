package com.fulfilment.application.monolith.fulfilmentunits.adapters.stores;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.adapters.database.StoreRepository;
import org.junit.jupiter.api.Test;

class StoreLookupAdapterTest {

  @Test
  void existsByIdShouldReturnTrueWhenStoreExists() {
    StoreRepository repository = mock(StoreRepository.class);
    when(repository.findById(1L)).thenReturn(new Store("TONSTAD"));
    StoreLookupAdapter adapter = new StoreLookupAdapter(repository);

    assertTrue(adapter.existsById(1L));
  }

  @Test
  void existsByIdShouldReturnFalseWhenStoreMissing() {
    StoreRepository repository = mock(StoreRepository.class);
    when(repository.findById(1L)).thenReturn(null);
    StoreLookupAdapter adapter = new StoreLookupAdapter(repository);

    assertFalse(adapter.existsById(1L));
  }
}
