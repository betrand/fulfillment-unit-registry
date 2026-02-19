package com.fulfilment.application.monolith.fulfilmentunits;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfilmentAssociationRepository implements PanacheRepository<FulfilmentAssociation> {

  public boolean existsAssociation(Long productId, Long storeId, String warehouseBusinessUnitCode) {
    return count(
            "productId = ?1 and storeId = ?2 and warehouseBusinessUnitCode = ?3",
            productId,
            storeId,
            warehouseBusinessUnitCode)
        > 0;
  }

  public boolean existsWarehouseForStore(Long storeId, String warehouseBusinessUnitCode) {
    return count("storeId = ?1 and warehouseBusinessUnitCode = ?2", storeId, warehouseBusinessUnitCode)
        > 0;
  }

  public boolean existsProductForWarehouse(String warehouseBusinessUnitCode, Long productId) {
    return count("warehouseBusinessUnitCode = ?1 and productId = ?2", warehouseBusinessUnitCode, productId)
        > 0;
  }

  public long countDistinctWarehousesForProductAndStore(Long productId, Long storeId) {
    return getEntityManager()
        .createQuery(
            "select count(distinct a.warehouseBusinessUnitCode) "
                + "from FulfilmentAssociation a "
                + "where a.productId = :productId and a.storeId = :storeId",
            Long.class)
        .setParameter("productId", productId)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  public long countDistinctWarehousesForStore(Long storeId) {
    return getEntityManager()
        .createQuery(
            "select count(distinct a.warehouseBusinessUnitCode) "
                + "from FulfilmentAssociation a "
                + "where a.storeId = :storeId",
            Long.class)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  public long countDistinctProductsForWarehouse(String warehouseBusinessUnitCode) {
    return getEntityManager()
        .createQuery(
            "select count(distinct a.productId) "
                + "from FulfilmentAssociation a "
                + "where a.warehouseBusinessUnitCode = :warehouseBusinessUnitCode",
            Long.class)
        .setParameter("warehouseBusinessUnitCode", warehouseBusinessUnitCode)
        .getSingleResult();
  }
}

