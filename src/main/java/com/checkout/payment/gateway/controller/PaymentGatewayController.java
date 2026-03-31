package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("api")
public class PaymentGatewayController {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayController.class);


  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }


  @GetMapping("/payment/{id}")
  public ResponseEntity<PaymentResponse> getPostPaymentEventById(@PathVariable UUID id) {
    LOG.info("Starting payment processing for paymentId: {}",id);

    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest paymentRequest) {
   //TODO: instead of random UUID key could be derived from payload variables, this allows ID to act as Idempotency key
    UUID paymentId = UUID.randomUUID();
    LOG.info("Starting payment processing for paymentId: {}, amount: {} {}",
        paymentId, paymentRequest.amount(), paymentRequest.currency());

    return new ResponseEntity<>(paymentGatewayService.processPayment(paymentRequest,paymentId), HttpStatus.OK);
  }

}
