package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.enums.PaymentStatus;
import java.util.UUID;

public class Payment {

  private final UUID id;
  private final PaymentRequest paymentRequest;
  private BankResponse bankResponse;
  private PaymentStatus status;


  private Payment(PaymentRequest request) {
    this.id = UUID.randomUUID();
    this.paymentRequest = request;
    this.status = PaymentStatus.AUTHORIZED; //TODO: UPDATE THE TO USE ENUM
  }

  public static Payment fromPaymentRequest(PaymentRequest request) {
    return new
        Payment(request);
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

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public void updateWithBankResponse(BankResponse bankResponse) {
    this.bankResponse = bankResponse;
    if(bankResponse.getAuthorized()==null) throw new RuntimeException("State should never be null");
    if (bankResponse.getAuthorized()) {
      status = PaymentStatus.AUTHORIZED;
    } else{
      status = PaymentStatus.DECLINED;
    }
  }
}