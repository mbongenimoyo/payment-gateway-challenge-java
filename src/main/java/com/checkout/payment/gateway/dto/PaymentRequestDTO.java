package com.checkout.payment.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDTO(@JsonProperty("card_number") @NotBlank @NotNull String cardNumber,
                                @JsonProperty("expiry_month") @NotNull Integer expiryMonth,
                                @JsonProperty("expiry_year") @NotNull Integer expiryYear,
                                @JsonProperty("currency") @NotNull @NotBlank String currency,
                                @JsonProperty("amount") @NotNull Long amount,
                                @JsonProperty("cvv") @NotBlank @NotNull String cvv)  {


  @JsonProperty("expiry_date")
  public String getExpiryDate() {
    return String.format("%d/%d", expiryMonth, expiryYear);
  }
}