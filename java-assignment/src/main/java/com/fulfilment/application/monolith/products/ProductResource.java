package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.products.application.usecases.ProductUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductUseCase productUseCase;

  @GET
  public List<Product> get() {
    return productUseCase.list();
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    return productUseCase.getSingle(id);
  }

  @POST
  public Response create(Product product) {
    productUseCase.create(product);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  public Product update(Long id, Product product) {
    return productUseCase.update(id, product);
  }

  @DELETE
  @Path("{id}")
  public Response delete(Long id) {
    productUseCase.delete(id);
    return Response.status(204).build();
  }
}
