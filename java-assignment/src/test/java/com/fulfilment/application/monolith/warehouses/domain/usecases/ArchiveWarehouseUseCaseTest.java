package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archiveShouldSetArchivedAtAndUpdateStore() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";

    useCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  void archiveShouldFailWhenWarehouseIsNull() {
    assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
    verifyNoInteractions(warehouseStore);
  }

  @Test
  void archiveShouldFailWhenAlreadyArchived() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.archivedAt = LocalDateTime.now();

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(warehouse));
    verifyNoInteractions(warehouseStore);
  }
}
