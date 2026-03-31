package com.checkout.payment.gateway.model.domain;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.util.CardValidationUtil;
import com.checkout.payment.gateway.util.CurrencyUtil;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @param cvv TODO: change CVV to string - int does not support leading '0'
 */ //Business logic
public record PaymentRequest(String cardNumberLastFour, int expiryMonth, int expiryYear,
                             String currency, long amount, String cvv) implements Serializable {

  public PaymentRequest {
    validate(cardNumberLastFour, expiryMonth, expiryYear, amount, cvv, currency);
    cardNumberLastFour =  cardNumberLastFour.substring(cardNumberLastFour.length()-4);
  }

  public static PaymentRequest fromDTO(CreatePaymentRequest dto) {

    return new PaymentRequest(
        dto.cardNumber(),
        dto.expiryMonth(),
        dto.expiryYear(),
        dto.currency(),
        dto.amount(),
        dto.cvv()
    );
  }

  private void validate(String cardNumber, int expiryMonth, int expiryYear, long amount,
      String cvv, String currency) throws InvalidPaymentException {

    Set<String> errors = new HashSet<>();
    //TODO: add proper handling for these exceptions
    if (!CardValidationUtil.isValidCardNumber(cardNumber)) {
      errors.add("Invalid Card Details");
    }
    if (!CardValidationUtil.isValidCVV(cvv)) {
      errors.add("Invalid Card Details");
    }
    if (expiryMonth < 1 || expiryMonth > 12) {
      errors.add("Invalid expiry month");
    }
    if (amount <= 0) {
      errors.add("Invalid amount");
    }

    if(!CurrencyUtil.isValidCurrency(currency)){
      errors.add("Invalid currency");
    }

    if (!CardValidationUtil.isValidExpiryDate(expiryMonth, expiryYear)) {
      errors.add("Card has expired");
    }

    if(!errors.isEmpty()) throw  new InvalidPaymentException(errors);
  }



  @Override
  public String toString() {
    return "PostPaymentRequest{" +
        "cardNumber=" + cardNumberLastFour +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=" + cvv +
        '}';
  }
}
