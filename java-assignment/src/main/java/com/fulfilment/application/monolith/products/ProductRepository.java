package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import io.quarkus.panache.common.Sort;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product>, ProductStore {

  @Override
  public List<Product> listAllByName() {
    return listAll(Sort.by("name"));
  }

  @Override
  public Product findById(Long id) {
    return find("id", id).firstResult();
  }

  @Override
  public void create(Product product) {
    persist(product);
  }

  @Override
  public void remove(Product product) {
    delete("id", product.id);
  }
}
