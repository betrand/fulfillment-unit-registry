package com.fulfilment.application.monolith.products.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductUseCaseTest {

  private ProductStore productStore;
  private ProductUseCase useCase;

  @BeforeEach
  void setUp() {
    productStore = mock(ProductStore.class);
    useCase = new ProductUseCase(productStore);
  }

  @Test
  void listShouldDelegateToStore() {
    List<Product> products = List.of(new Product("P1"));
    when(productStore.listAllByName()).thenReturn(products);

    assertSame(products, useCase.list());
  }

  @Test
  void getSingleShouldFailWhenProductNotFound() {
    when(productStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.getSingle(1L));
  }

  @Test
  void createShouldFailWhenIdIsProvided() {
    Product product = new Product("P1");
    product.id = 1L;

    assertThrows(UnprocessableEntityException.class, () -> useCase.create(product));
  }

  @Test
  void createShouldPersistWhenValid() {
    Product product = new Product("P1");

    Product created = useCase.create(product);

    assertSame(product, created);
    verify(productStore).create(product);
  }

  @Test
  void updateShouldFailWhenNameIsMissing() {
    Product product = new Product();

    assertThrows(UnprocessableEntityException.class, () -> useCase.update(1L, product));
  }

  @Test
  void updateShouldFailWhenProductNotFound() {
    Product product = new Product("P1");
    when(productStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.update(1L, product));
  }

  @Test
  void updateShouldMutateExistingEntity() {
    Product existing = new Product("OLD");
    existing.description = "old";
    existing.stock = 2;
    Product incoming = new Product("NEW");
    incoming.description = "new";
    incoming.stock = 10;

    when(productStore.findById(1L)).thenReturn(existing);

    Product updated = useCase.update(1L, incoming);

    assertSame(existing, updated);
    assertEquals("NEW", existing.name);
    assertEquals("new", existing.description);
    assertEquals(10, existing.stock);
  }

  @Test
  void deleteShouldFailWhenProductNotFound() {
    when(productStore.findById(1L)).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.delete(1L));
  }

  @Test
  void deleteShouldRemoveProductWhenFound() {
    Product existing = new Product("P1");
    when(productStore.findById(1L)).thenReturn(existing);

    useCase.delete(1L);

    verify(productStore).remove(existing);
  }
}
