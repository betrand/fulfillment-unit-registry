package com.fulfilment.application.monolith.products.application.usecases;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductUseCase {

  private final ProductStore productStore;

  public ProductUseCase(ProductStore productStore) {
    this.productStore = productStore;
  }

  public List<Product> list() {
    return productStore.listAllByName();
  }

  public Product getSingle(Long id) {
    Product entity = productStore.findById(id);
    if (entity == null) {
      throw new NotFoundException("Product with id of " + id + " does not exist.");
    }
    return entity;
  }

  @Transactional
  public Product create(Product product) {
    if (product.id != null) {
      throw new UnprocessableEntityException("Id was invalidly set on request.");
    }

    productStore.create(product);
    return product;
  }

  @Transactional
  public Product update(Long id, Product product) {
    if (product.name == null) {
      throw new UnprocessableEntityException("Product Name was not set on request.");
    }

    Product entity = productStore.findById(id);

    if (entity == null) {
      throw new NotFoundException("Product with id of " + id + " does not exist.");
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    return entity;
  }

  @Transactional
  public void delete(Long id) {
    Product entity = productStore.findById(id);
    if (entity == null) {
      throw new NotFoundException("Product with id of " + id + " does not exist.");
    }
    productStore.remove(entity);
  }
}
