package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.dto.PaymentRequestDTO;
import com.checkout.payment.gateway.exception.BankProcessingException;
import com.checkout.payment.gateway.model.BankResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class BankService {

  private static final Logger LOG = LoggerFactory.getLogger(BankService.class);
  private final RestTemplate restTemplate;

  @Value("${bank.uri}")
  private String bankUrl;

  public BankService( @Autowired RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public BankResponse processPayment(PaymentRequestDTO request)  {
    LOG.debug("processPayment:: started bank call for payment request: {}", request.toString());
    try {
      // Prepare headers
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      // Create request entity
      HttpEntity<PaymentRequestDTO> entity = new HttpEntity<>(request, headers);

      // Make POST request to bank simulator
      ResponseEntity<BankResponse> response = restTemplate.exchange(
          bankUrl,
          HttpMethod.POST,
          entity,
          BankResponse.class
      );
      LOG.debug("processPayment:: ...finished calling bank");
      return response.getBody();
    } catch (HttpClientErrorException | HttpServerErrorException e) {
      LOG.error("processPayment:: exception was thrown for request: {}", request.toString());
      throw new BankProcessingException("Failed to communicate with bank: " + e.getMessage());
    }
  }

}
