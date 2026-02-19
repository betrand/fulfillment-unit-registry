package com.fulfilment.application.monolith.fulfilmentunits;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("fulfilment-association")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentAssociationResource {

  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;
  @Inject FulfilmentAssociationRepository fulfilmentAssociationRepository;

  private static final Logger LOGGER =
      Logger.getLogger(FulfilmentAssociationResource.class.getName());

  @GET
  public List<FulfilmentAssociation> list() {
    return fulfilmentAssociationRepository.listAll(Sort.by("id"));
  }

  @POST
  @Transactional
  public Response associate(FulfilmentAssociationRequest request) {
    validateRequest(request);

    if (productRepository.findById(request.productId) == null) {
      throw new WebApplicationException(
          "Product with id of " + request.productId + " does not exist.", 404);
    }

    if (Store.findById(request.storeId) == null) {
      throw new WebApplicationException("Store with id of " + request.storeId + " does not exist.", 404);
    }

    var resolvedWarehouse = warehouseRepository.findByBusinessUnitCode(request.warehouseIdentifier);
    if (resolvedWarehouse == null) {
      resolvedWarehouse = warehouseRepository.findById(request.warehouseIdentifier);
    }
    if (resolvedWarehouse == null) {
      throw new WebApplicationException(
          "Warehouse with identifier " + request.warehouseIdentifier + " does not exist.", 404);
    }

    String warehouseBusinessUnitCode = resolvedWarehouse.businessUnitCode;

    if (fulfilmentAssociationRepository.existsAssociation(
        request.productId, request.storeId, warehouseBusinessUnitCode)) {
      throw new WebApplicationException(
          "Association already exists for product/store/warehouse combination.", 409);
    }

    long warehousesForProductAndStore =
        fulfilmentAssociationRepository.countDistinctWarehousesForProductAndStore(
            request.productId, request.storeId);
    if (warehousesForProductAndStore >= 2) {
      throw new WebApplicationException(
          "A product can be fulfilled by at most 2 warehouses per store.", 400);
    }

    if (!fulfilmentAssociationRepository.existsWarehouseForStore(
        request.storeId, warehouseBusinessUnitCode)) {
      long warehousesForStore =
          fulfilmentAssociationRepository.countDistinctWarehousesForStore(request.storeId);
      if (warehousesForStore >= 3) {
        throw new WebApplicationException(
            "A store can be fulfilled by at most 3 warehouses.", 400);
      }
    }

    if (!fulfilmentAssociationRepository.existsProductForWarehouse(
        warehouseBusinessUnitCode, request.productId)) {
      long productsForWarehouse =
          fulfilmentAssociationRepository.countDistinctProductsForWarehouse(
              warehouseBusinessUnitCode);
      if (productsForWarehouse >= 5) {
        throw new WebApplicationException(
            "A warehouse can store at most 5 product types.", 400);
      }
    }

    FulfilmentAssociation association = new FulfilmentAssociation();
    association.productId = request.productId;
    association.storeId = request.storeId;
    association.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    fulfilmentAssociationRepository.persist(association);

    return Response.ok(association).status(201).build();
  }

  private void validateRequest(FulfilmentAssociationRequest request) {
    if (request == null) {
      throw new WebApplicationException("Request body is required.", 422);
    }
    if (request.productId == null) {
      throw new WebApplicationException("productId is required.", 422);
    }
    if (request.storeId == null) {
      throw new WebApplicationException("storeId is required.", 422);
    }
    if (request.warehouseIdentifier == null || request.warehouseIdentifier.isBlank()) {
      throw new WebApplicationException("warehouseIdentifier is required.", 422);
    }
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}

