package com.fulfilment.application.monolith.shared.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.shared.application.exceptions.ApplicationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalErrorMapper implements ExceptionMapper<Exception> {

  private static final Logger LOGGER = Logger.getLogger(GlobalErrorMapper.class.getName());

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(Exception exception) {
    LOGGER.error("Failed to handle request", exception);

    int statusCode = resolveStatusCode(exception);

    ObjectNode exceptionJson = objectMapper.createObjectNode();
    exceptionJson.put("exceptionType", exception.getClass().getName());
    exceptionJson.put("code", statusCode);

    if (exception.getMessage() != null) {
      exceptionJson.put("error", exception.getMessage());
    }

    return Response.status(statusCode).entity(exceptionJson).build();
  }

  private int resolveStatusCode(Exception exception) {
    if (exception instanceof ApplicationException) {
      return ((ApplicationException) exception).getStatusCode();
    }

    if (exception instanceof WebApplicationException) {
      return ((WebApplicationException) exception).getResponse().getStatus();
    }

    return 500;
  }
}
