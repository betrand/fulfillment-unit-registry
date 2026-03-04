package com.fulfilment.application.monolith.fulfilmentunits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfilmentunits.application.usecases.FulfilmentAssociationUseCase;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FulfilmentAssociationResourceUnitTest {

  private FulfilmentAssociationUseCase useCase;
  private FulfilmentAssociationResource resource;

  @BeforeEach
  void setUp() {
    useCase = mock(FulfilmentAssociationUseCase.class);
    resource = new FulfilmentAssociationResource();
    resource.fulfilmentAssociationUseCase = useCase;
  }

  @Test
  void listShouldDelegateToUseCase() {
    List<FulfilmentAssociation> associations = List.of(new FulfilmentAssociation());
    when(useCase.list()).thenReturn(associations);

    assertSame(associations, resource.list());
  }

  @Test
  void associateShouldDelegateAndReturnCreatedResponse() {
    FulfilmentAssociationRequest request = new FulfilmentAssociationRequest();
    FulfilmentAssociation association = new FulfilmentAssociation();
    when(useCase.associate(request)).thenReturn(association);

    var response = resource.associate(request);

    assertEquals(201, response.getStatus());
    assertSame(association, response.getEntity());
    verify(useCase).associate(request);
  }
}
