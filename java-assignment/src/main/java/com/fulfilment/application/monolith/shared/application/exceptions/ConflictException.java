package com.fulfilment.application.monolith.shared.application.exceptions;

public class ConflictException extends ApplicationException {

  public ConflictException(String message) {
    super(message, 409);
  }
}
