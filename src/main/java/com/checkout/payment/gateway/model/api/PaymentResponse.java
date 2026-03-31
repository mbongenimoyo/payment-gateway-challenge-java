package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

public record PaymentResponse(@JsonProperty("id") UUID id,
                              @JsonProperty("status") PaymentStatus status,
                              @JsonProperty("card_number_last_four") String cardNumberLastFour,
                              @JsonProperty("expiry_month")Integer expiryMonth,
                              @JsonProperty("expiry_year") Integer expiryYear,
                              @JsonProperty("currency") String currency,
                              @JsonProperty("amount") Long amount) implements Serializable {

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }
}
