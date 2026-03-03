package com.fulfilment.application.monolith.shared.application.exceptions;

public class UnprocessableEntityException extends ApplicationException {

  public UnprocessableEntityException(String message) {
    super(message, 422);
  }
}
