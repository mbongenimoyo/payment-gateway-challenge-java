package com.checkout.payment.gateway.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {

  private UUID paymentId;

  public PaymentNotFoundException(String message, UUID id) {
    super(message);
    this.paymentId = id;
  }
}
