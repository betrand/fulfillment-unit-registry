package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductRepositoryTest {

  @Inject ProductRepository productRepository;

  @Test
  @TestTransaction
  void shouldCreateFindListAndRemoveProduct() {
    Product alpha = new Product("ALPHA-" + System.nanoTime());
    alpha.stock = 10;
    productRepository.create(alpha);

    Product beta = new Product("BETA-" + System.nanoTime());
    beta.stock = 5;
    productRepository.create(beta);

    assertNotNull(alpha.id);
    assertNotNull(beta.id);
    assertNotNull(productRepository.findById(alpha.id));

    var list = productRepository.listAllByName();
    assertTrue(list.stream().anyMatch(p -> alpha.name.equals(p.name)));
    assertTrue(list.stream().anyMatch(p -> beta.name.equals(p.name)));

    productRepository.remove(alpha);
    assertNull(productRepository.findById(alpha.id));
    assertNotNull(productRepository.findById(beta.id));
    assertEquals(beta.name, productRepository.findById(beta.id).name);
  }
}
