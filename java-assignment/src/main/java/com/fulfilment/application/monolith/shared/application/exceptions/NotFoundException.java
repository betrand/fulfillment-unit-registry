package com.fulfilment.application.monolith.shared.application.exceptions;

public class NotFoundException extends ApplicationException {

  public NotFoundException(String message) {
    super(message, 404);
  }
}
