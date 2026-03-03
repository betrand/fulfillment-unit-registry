package com.fulfilment.application.monolith.fulfilmentunits.adapters.products;

import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.ProductLookupPort;
import com.fulfilment.application.monolith.products.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductLookupAdapter implements ProductLookupPort {

  private final ProductRepository productRepository;

  public ProductLookupAdapter(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public boolean existsById(Long productId) {
    return productRepository.findById(productId) != null;
  }
}
