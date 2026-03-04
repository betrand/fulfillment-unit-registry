package com.fulfilment.application.monolith.fulfilmentunits.adapters.products;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import org.junit.jupiter.api.Test;

class ProductLookupAdapterTest {

  @Test
  void existsByIdShouldReturnTrueWhenProductExists() {
    ProductRepository repository = mock(ProductRepository.class);
    when(repository.findById(1L)).thenReturn(new Product("TONSTAD"));
    ProductLookupAdapter adapter = new ProductLookupAdapter(repository);

    assertTrue(adapter.existsById(1L));
  }

  @Test
  void existsByIdShouldReturnFalseWhenProductMissing() {
    ProductRepository repository = mock(ProductRepository.class);
    when(repository.findById(1L)).thenReturn(null);
    ProductLookupAdapter adapter = new ProductLookupAdapter(repository);

    assertFalse(adapter.existsById(1L));
  }
}
