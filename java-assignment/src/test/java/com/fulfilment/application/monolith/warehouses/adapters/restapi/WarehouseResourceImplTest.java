package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class WarehouseResourceImplTest {

  private WarehouseRepository warehouseRepository;
  private CreateWarehouseUseCase createWarehouseUseCase;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  private WarehouseResourceImpl resource;

  @BeforeEach
  void setUp() {
    warehouseRepository = Mockito.mock(WarehouseRepository.class);
    createWarehouseUseCase = Mockito.mock(CreateWarehouseUseCase.class);
    archiveWarehouseUseCase = Mockito.mock(ArchiveWarehouseUseCase.class);
    replaceWarehouseUseCase = Mockito.mock(ReplaceWarehouseUseCase.class);
    resource = new WarehouseResourceImpl();
    setField(resource, "warehouseRepository", warehouseRepository);
    setField(resource, "createWarehouseUseCase", createWarehouseUseCase);
    setField(resource, "archiveWarehouseUseCase", archiveWarehouseUseCase);
    setField(resource, "replaceWarehouseUseCase", replaceWarehouseUseCase);
  }

  @Test
  void listAllShouldMapDomainWarehousesToApiResponse() {
    when(warehouseRepository.getAll())
        .thenReturn(List.of(domainWarehouse("MWH.001", "ZWOLLE-001", 30, 10)));

    List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();

    assertEquals(1, result.size());
    assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
    assertEquals("MWH.001", result.get(0).getId());
  }

  @Test
  void createShouldDelegateToUseCaseAndReturnCreatedWarehouse() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("MWH.002", "EINDHOVEN-001", 50, 20);
    doNothing().when(createWarehouseUseCase).create(any(Warehouse.class));

    com.warehouse.api.beans.Warehouse response = resource.createANewWarehouseUnit(request);

    assertEquals("MWH.002", response.getBusinessUnitCode());
    assertEquals("EINDHOVEN-001", response.getLocation());
    verify(createWarehouseUseCase).create(any(Warehouse.class));
  }

  @Test
  void createShouldMapValidationFailureToBadRequest() {
    doThrow(new IllegalArgumentException("invalid")).when(createWarehouseUseCase).create(any(Warehouse.class));

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () -> resource.createANewWarehouseUnit(apiWarehouse("MWH.003", "UNKNOWN", 1, 1)));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void getByIdShouldFallbackToNumericIdLookupWhenBuCodeNotFound() {
    when(warehouseRepository.findByBusinessUnitCode("12")).thenReturn(null);
    when(warehouseRepository.findById("12"))
        .thenReturn(domainWarehouse("MWH.012", "AMSTERDAM-001", 50, 5));

    com.warehouse.api.beans.Warehouse response = resource.getAWarehouseUnitByID("12");

    assertEquals("MWH.012", response.getBusinessUnitCode());
  }

  @Test
  void getByIdShouldReturnNotFoundWhenWarehouseMissing() {
    when(warehouseRepository.findByBusinessUnitCode("MISSING")).thenReturn(null);
    when(warehouseRepository.findById("MISSING")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("MISSING"));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void archiveShouldDelegateToUseCaseWhenWarehouseExists() {
    Warehouse warehouse = domainWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);

    resource.archiveAWarehouseUnitByID("MWH.001");

    verify(archiveWarehouseUseCase).archive(warehouse);
  }

  @Test
  void archiveShouldReturnNotFoundWhenWarehouseMissing() {
    when(warehouseRepository.findByBusinessUnitCode("MISSING")).thenReturn(null);
    when(warehouseRepository.findById("MISSING")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("MISSING"));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void archiveShouldMapIllegalArgumentToBadRequest() {
    Warehouse warehouse = domainWarehouse("MWH.001", "ZWOLLE-001", 30, 10);
    when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(warehouse);
    doThrow(new IllegalArgumentException("already archived"))
        .when(archiveWarehouseUseCase)
        .archive(warehouse);

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("MWH.001"));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void replaceShouldSetBusinessUnitCodeAndReturnUpdatedWarehouse() {
    com.warehouse.api.beans.Warehouse request = apiWarehouse("IGNORED", "AMSTERDAM-001", 60, 5);
    Warehouse persisted = domainWarehouse("MWH.050", "AMSTERDAM-001", 60, 5);
    when(warehouseRepository.findByBusinessUnitCode("MWH.050")).thenReturn(persisted);

    com.warehouse.api.beans.Warehouse response =
        resource.replaceTheCurrentActiveWarehouse("MWH.050", request);

    ArgumentCaptor<Warehouse> warehouseCaptor = ArgumentCaptor.forClass(Warehouse.class);
    verify(replaceWarehouseUseCase).replace(warehouseCaptor.capture());
    Warehouse replacementPayload = warehouseCaptor.getValue();
    assertEquals("MWH.050", replacementPayload.businessUnitCode);
    assertNotNull(response);
    assertEquals("MWH.050", response.getBusinessUnitCode());
  }

  @Test
  void replaceShouldMapNotFoundExceptionTo404() {
    doThrow(new NoSuchElementException("missing"))
        .when(replaceWarehouseUseCase)
        .replace(any(Warehouse.class));

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.replaceTheCurrentActiveWarehouse(
                    "MWH.404", apiWarehouse("MWH.404", "ZWOLLE-001", 30, 5)));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void replaceShouldMapIllegalArgumentExceptionTo400() {
    doThrow(new IllegalArgumentException("invalid"))
        .when(replaceWarehouseUseCase)
        .replace(any(Warehouse.class));

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.replaceTheCurrentActiveWarehouse(
                    "MWH.400", apiWarehouse("MWH.400", "UNKNOWN-001", 30, 5)));

    assertEquals(400, exception.getResponse().getStatus());
  }

  private com.warehouse.api.beans.Warehouse apiWarehouse(
      String buCode, String location, int capacity, int stock) {
    com.warehouse.api.beans.Warehouse warehouse = new com.warehouse.api.beans.Warehouse();
    warehouse.setBusinessUnitCode(buCode);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    return warehouse;
  }

  private Warehouse domainWarehouse(String buCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to set field " + fieldName, e);
    }
  }
}
