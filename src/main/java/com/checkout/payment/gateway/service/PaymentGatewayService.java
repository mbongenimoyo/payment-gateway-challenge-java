package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.PaymentRequestDTO;
import com.checkout.payment.gateway.dto.PaymentResponseDTO;
import com.checkout.payment.gateway.exception.BankProcessingException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.Payment;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankService bankService;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      BankService bankService) {
    this.paymentsRepository = paymentsRepository;
    this.bankService = bankService;
  }

  public PaymentResponseDTO getPaymentById(UUID id) throws PaymentNotFoundException {
    LOG.debug("Requesting access to to payment with ID {}", id);
     return paymentsRepository.get(id)
        .map(Payment::toPaymentResponseDTO)
        .orElseThrow(() -> new PaymentNotFoundException("could not find payment",id));
  }

  public PaymentResponseDTO processPayment(PaymentRequestDTO requestDTO,UUID paymentId) {
    LOG.debug("Requesting access to to payment with ID ");
    Payment payment = new Payment(PaymentRequest.fromDTO(requestDTO),paymentId);
    BankResponse bankResponse = null;
    try{
       bankResponse = bankService.processPayment(requestDTO);
    } catch (BankProcessingException ex){
      payment.markAsRejected("Bank processing failed: " + ex.getMessage());
      paymentsRepository.add(payment);
      throw  ex;
    }

    payment.updateWithBankResponse(bankResponse);
    paymentsRepository.add(payment);

    return payment.toPaymentResponseDTO();
  }
}
