package com.checkout.payment.gateway.exception;

import java.util.Set;

public class InvalidPaymentException extends RuntimeException {
  private final Set<String> errors;
  public InvalidPaymentException(Set<String> errors) {
   this.errors =errors;
  }

  public Set<String> getErrors() {
    return errors;
  }
}
