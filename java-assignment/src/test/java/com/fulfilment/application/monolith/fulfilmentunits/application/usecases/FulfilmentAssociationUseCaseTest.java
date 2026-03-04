package com.fulfilment.application.monolith.fulfilmentunits.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfilmentunits.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilmentunits.FulfilmentAssociationRequest;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.FulfilmentAssociationStore;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.ProductLookupPort;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.StoreLookupPort;
import com.fulfilment.application.monolith.fulfilmentunits.domain.ports.WarehouseLookupPort;
import com.fulfilment.application.monolith.shared.application.exceptions.BadRequestException;
import com.fulfilment.application.monolith.shared.application.exceptions.ConflictException;
import com.fulfilment.application.monolith.shared.application.exceptions.NotFoundException;
import com.fulfilment.application.monolith.shared.application.exceptions.UnprocessableEntityException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FulfilmentAssociationUseCaseTest {

  private ProductLookupPort productLookupPort;
  private StoreLookupPort storeLookupPort;
  private WarehouseLookupPort warehouseLookupPort;
  private FulfilmentAssociationStore fulfilmentAssociationStore;
  private FulfilmentAssociationUseCase useCase;

  @BeforeEach
  void setUp() {
    productLookupPort = mock(ProductLookupPort.class);
    storeLookupPort = mock(StoreLookupPort.class);
    warehouseLookupPort = mock(WarehouseLookupPort.class);
    fulfilmentAssociationStore = mock(FulfilmentAssociationStore.class);
    useCase =
        new FulfilmentAssociationUseCase(
            productLookupPort, storeLookupPort, warehouseLookupPort, fulfilmentAssociationStore);
  }

  @Test
  void listShouldDelegateToStore() {
    List<FulfilmentAssociation> associations = List.of(new FulfilmentAssociation());
    when(fulfilmentAssociationStore.listAllById()).thenReturn(associations);

    assertSame(associations, useCase.list());
  }

  @Test
  void associateShouldFailWhenRequestIsMissing() {
    assertThrows(UnprocessableEntityException.class, () -> useCase.associate(null));
  }

  @Test
  void associateShouldFailWhenProductIdMissing() {
    FulfilmentAssociationRequest request = validRequest();
    request.productId = null;

    assertThrows(UnprocessableEntityException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenStoreIdMissing() {
    FulfilmentAssociationRequest request = validRequest();
    request.storeId = null;

    assertThrows(UnprocessableEntityException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenWarehouseIdentifierMissing() {
    FulfilmentAssociationRequest request = validRequest();
    request.warehouseIdentifier = " ";

    assertThrows(UnprocessableEntityException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenProductDoesNotExist() {
    FulfilmentAssociationRequest request = validRequest();
    when(productLookupPort.existsById(1L)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenStoreDoesNotExist() {
    FulfilmentAssociationRequest request = validRequest();
    when(productLookupPort.existsById(1L)).thenReturn(true);
    when(storeLookupPort.existsById(2L)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenWarehouseDoesNotExist() {
    FulfilmentAssociationRequest request = validRequest();
    when(productLookupPort.existsById(1L)).thenReturn(true);
    when(storeLookupPort.existsById(2L)).thenReturn(true);
    when(warehouseLookupPort.resolveBusinessUnitCode("W-1")).thenReturn(null);

    assertThrows(NotFoundException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenAssociationAlreadyExists() {
    FulfilmentAssociationRequest request = validRequest();
    mockLookupSuccess(request);
    when(fulfilmentAssociationStore.existsAssociation(1L, 2L, "BU-1")).thenReturn(true);

    assertThrows(ConflictException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenProductStoreHasTwoWarehousesAlready() {
    FulfilmentAssociationRequest request = validRequest();
    mockLookupSuccess(request);
    when(fulfilmentAssociationStore.existsAssociation(1L, 2L, "BU-1")).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctWarehousesForProductAndStore(1L, 2L)).thenReturn(2L);

    assertThrows(BadRequestException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenStoreWouldExceedThreeWarehouses() {
    FulfilmentAssociationRequest request = validRequest();
    mockLookupSuccess(request);
    when(fulfilmentAssociationStore.existsAssociation(1L, 2L, "BU-1")).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctWarehousesForProductAndStore(1L, 2L)).thenReturn(1L);
    when(fulfilmentAssociationStore.existsWarehouseForStore(2L, "BU-1")).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctWarehousesForStore(2L)).thenReturn(3L);

    assertThrows(BadRequestException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldFailWhenWarehouseWouldExceedFiveProductTypes() {
    FulfilmentAssociationRequest request = validRequest();
    mockLookupSuccess(request);
    when(fulfilmentAssociationStore.existsAssociation(1L, 2L, "BU-1")).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctWarehousesForProductAndStore(1L, 2L)).thenReturn(1L);
    when(fulfilmentAssociationStore.existsWarehouseForStore(2L, "BU-1")).thenReturn(true);
    when(fulfilmentAssociationStore.existsProductForWarehouse("BU-1", 1L)).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctProductsForWarehouse("BU-1")).thenReturn(5L);

    assertThrows(BadRequestException.class, () -> useCase.associate(request));
  }

  @Test
  void associateShouldPersistAssociationWhenAllRulesPass() {
    FulfilmentAssociationRequest request = validRequest();
    mockLookupSuccess(request);
    when(fulfilmentAssociationStore.existsAssociation(1L, 2L, "BU-1")).thenReturn(false);
    when(fulfilmentAssociationStore.countDistinctWarehousesForProductAndStore(1L, 2L)).thenReturn(1L);
    when(fulfilmentAssociationStore.existsWarehouseForStore(2L, "BU-1")).thenReturn(true);
    when(fulfilmentAssociationStore.existsProductForWarehouse("BU-1", 1L)).thenReturn(true);

    FulfilmentAssociation association = useCase.associate(request);

    assertEquals(1L, association.productId);
    assertEquals(2L, association.storeId);
    assertEquals("BU-1", association.warehouseBusinessUnitCode);
    verify(fulfilmentAssociationStore).create(any(FulfilmentAssociation.class));
  }

  private FulfilmentAssociationRequest validRequest() {
    FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
    request.productId = 1L;
    request.storeId = 2L;
    request.warehouseIdentifier = "W-1";
    return request;
  }

  private void mockLookupSuccess(FulfilmentAssociationRequest request) {
    when(productLookupPort.existsById(request.productId)).thenReturn(true);
    when(storeLookupPort.existsById(request.storeId)).thenReturn(true);
    when(warehouseLookupPort.resolveBusinessUnitCode(request.warehouseIdentifier)).thenReturn("BU-1");
  }
}
