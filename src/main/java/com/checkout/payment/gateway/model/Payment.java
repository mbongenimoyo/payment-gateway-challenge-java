package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;


public class Payment {

  private final UUID id;
  private final PaymentRequest paymentRequest;
  private BankResponse bankResponse;
  private PaymentStatus status;


  public Payment(PaymentRequest request,UUID paymentId) {
    this.id = paymentId;
    this.paymentRequest = request;
    this.status = PaymentStatus.AUTHORIZED;
  }


  public PaymentResponseDTO toPaymentResponseDTO() {
    return new PaymentResponseDTO(
        this.id,
        this.status,
        this.paymentRequest.cardNumberLastFour(),
        this.paymentRequest.expiryMonth(),
        this.paymentRequest.expiryYear(),
        this.paymentRequest.currency(),
        this.paymentRequest.amount()
    );
  }

  public UUID getId() {
    return id;
  }

  public void markAsRejected(String reason) {
  this.status = PaymentStatus.REJECTED;
  this.bankResponse = null;
}

  public void updateWithBankResponse(BankResponse bankResponse) {
    this.bankResponse = bankResponse;
    if(bankResponse == null || bankResponse.getAuthorized()==null) {
      status = PaymentStatus.REJECTED;
      throw new RuntimeException("Could not process payment");
    }

    if (bankResponse.getAuthorized()) {
      status = PaymentStatus.AUTHORIZED;
    } else{
      status = PaymentStatus.DECLINED;
    }
  }
}