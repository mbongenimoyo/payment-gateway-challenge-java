package com.checkout.payment.gateway.model.bank;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;

public class BankRequest implements Serializable {

  @JsonProperty("card_number") @NotBlank
  @NotNull
  String cardNumber;

  @JsonProperty("currency")
  @NotNull
  @NotBlank String currency;

  @JsonProperty("amount")
  @NotNull Long amount;

  @JsonProperty("cvv")
  @NotBlank
  @NotNull String cvv;

  @JsonProperty("expiry_date")
  public String expiryDate;
  public BankRequest(CreatePaymentRequest paymentRequest){
    this.cardNumber = paymentRequest.cardNumber();
    this.currency = paymentRequest.currency();
    this.cvv = paymentRequest.cvv();
    this.amount = paymentRequest.amount();
    this.expiryDate = String.format("%d/%d", paymentRequest.expiryMonth(), paymentRequest.expiryYear());
  }

}
