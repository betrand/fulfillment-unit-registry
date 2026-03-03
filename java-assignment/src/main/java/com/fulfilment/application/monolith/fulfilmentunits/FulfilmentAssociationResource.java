package com.fulfilment.application.monolith.fulfilmentunits;

import com.fulfilment.application.monolith.fulfilmentunits.application.usecases.FulfilmentAssociationUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment-association")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentAssociationResource {

  @Inject FulfilmentAssociationUseCase fulfilmentAssociationUseCase;

  @GET
  public List<FulfilmentAssociation> list() {
    return fulfilmentAssociationUseCase.list();
  }

  @POST
  public Response associate(FulfilmentAssociationRequest request) {
    FulfilmentAssociation association = fulfilmentAssociationUseCase.associate(request);
    return Response.ok(association).status(201).build();
  }
}
