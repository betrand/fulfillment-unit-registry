package com.fulfilment.application.monolith.fulfilmentunits;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfilment_association",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_id", "store_id", "warehouse_business_unit_code"}))
public class FulfilmentAssociation {

  @Id @GeneratedValue public Long id;

  @Column(name = "product_id", nullable = false)
  public Long productId;

  @Column(name = "store_id", nullable = false)
  public Long storeId;

  @Column(name = "warehouse_business_unit_code", nullable = false)
  public String warehouseBusinessUnitCode;
}

