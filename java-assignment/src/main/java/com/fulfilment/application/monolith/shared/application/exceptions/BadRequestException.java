package com.fulfilment.application.monolith.shared.application.exceptions;

public class BadRequestException extends ApplicationException {

  public BadRequestException(String message) {
    super(message, 400);
  }
}
