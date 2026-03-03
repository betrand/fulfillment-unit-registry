package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.application.usecases.StoreUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject StoreUseCase storeUseCase;

  @GET
  public List<Store> get() {
    return storeUseCase.list();
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    return storeUseCase.getSingle(id);
  }

  @POST
  public Response create(Store store) {
    storeUseCase.create(store);
    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  public Store update(Long id, Store updatedStore) {
    return storeUseCase.update(id, updatedStore);
  }

  @PATCH
  @Path("{id}")
  public Store patch(Long id, Store updatedStore) {
    return storeUseCase.patch(id, updatedStore);
  }

  @DELETE
  @Path("{id}")
  public Response delete(Long id) {
    storeUseCase.delete(id);
    return Response.status(204).build();
  }
}
