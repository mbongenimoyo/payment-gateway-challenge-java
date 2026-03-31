package com.checkout.payment.gateway.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public record CreatePaymentRequest(@JsonProperty("card_number") @NotBlank @NotNull String cardNumber,
                                   @JsonProperty("expiry_month") @NotNull Integer expiryMonth,
                                   @JsonProperty("expiry_year") @NotNull Integer expiryYear,
                                   @JsonProperty("currency") @NotNull @NotBlank String currency,
                                   @JsonProperty("amount") @NotNull Long amount,
                                   @JsonProperty("cvv") @NotBlank @NotNull String cvv) implements
    Serializable {

}