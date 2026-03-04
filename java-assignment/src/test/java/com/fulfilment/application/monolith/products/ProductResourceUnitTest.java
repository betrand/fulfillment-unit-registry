package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.products.application.usecases.ProductUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductResourceUnitTest {

  private ProductUseCase productUseCase;
  private ProductResource resource;

  @BeforeEach
  void setUp() {
    productUseCase = mock(ProductUseCase.class);
    resource = new ProductResource();
    resource.productUseCase = productUseCase;
  }

  @Test
  void getShouldDelegateToUseCase() {
    List<Product> products = List.of(new Product("TONSTAD"));
    when(productUseCase.list()).thenReturn(products);

    assertSame(products, resource.get());
  }

  @Test
  void getSingleShouldDelegateToUseCase() {
    Product product = new Product("KALLAX");
    when(productUseCase.getSingle(1L)).thenReturn(product);

    assertSame(product, resource.getSingle(1L));
  }

  @Test
  void createShouldReturnCreatedStatusAndDelegateToUseCase() {
    Product product = new Product("BESTA");

    var response = resource.create(product);

    assertEquals(201, response.getStatus());
    verify(productUseCase).create(product);
  }

  @Test
  void updateShouldDelegateToUseCase() {
    Product incoming = new Product("NEW");
    Product updated = new Product("UPDATED");
    when(productUseCase.update(5L, incoming)).thenReturn(updated);

    assertSame(updated, resource.update(5L, incoming));
  }

  @Test
  void deleteShouldReturnNoContentAndDelegateToUseCase() {
    var response = resource.delete(7L);

    assertEquals(204, response.getStatus());
    verify(productUseCase).delete(7L);
  }
}
