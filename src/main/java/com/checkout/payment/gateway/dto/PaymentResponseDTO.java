package com.checkout.payment.gateway.dto;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record PaymentResponseDTO(@JsonProperty("id") UUID id,
                                 @JsonProperty("status") PaymentStatus status,
                                 @JsonProperty("card_number_last_four") String cardNumberLastFour,
                                 @JsonProperty("expiry_month")Integer expiryMonth,
                                 @JsonProperty("expiry_year") Integer expiryYear,
                                 @JsonProperty("currency") String currency,
                                 @JsonProperty("amount") Long amount) {

  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }
}
