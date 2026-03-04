package com.fulfilment.application.monolith.shared.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fulfilment.application.monolith.shared.application.exceptions.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GlobalErrorMapperTest {

  private GlobalErrorMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new GlobalErrorMapper();
    mapper.objectMapper = new ObjectMapper();
  }

  @Test
  void shouldMapApplicationExceptionUsingItsStatusCode() {
    Response response = mapper.toResponse(new BadRequestException("Invalid request"));

    assertEquals(400, response.getStatus());
  }

  @Test
  void shouldMapWebApplicationExceptionUsingHttpStatus() {
    Response response = mapper.toResponse(new WebApplicationException("Not found", 404));

    assertEquals(404, response.getStatus());
  }

  @Test
  void shouldMapUnexpectedExceptionsAsInternalServerError() {
    Response response = mapper.toResponse(new RuntimeException("Unexpected"));

    assertEquals(500, response.getStatus());
  }
}
