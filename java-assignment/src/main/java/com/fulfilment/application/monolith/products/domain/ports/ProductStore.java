package com.fulfilment.application.monolith.products.domain.ports;

import com.fulfilment.application.monolith.products.Product;
import java.util.List;

public interface ProductStore {

  List<Product> listAllByName();

  Product findById(Long id);

  void create(Product product);

  void remove(Product product);
}
