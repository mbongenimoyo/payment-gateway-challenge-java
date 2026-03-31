package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.exception.BankProcessingException;
import com.checkout.payment.gateway.model.bank.BankResponse;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);
  private final RestTemplate restTemplate;

  @Value("${bank.uri}")
  private String bankUrl;

  public BankClient(@Autowired RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  //
  public BankResponse processPayment(CreatePaymentRequest request)  {
    LOG.debug("processPayment:: started bank call for payment request: {}", request.toString());
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request, headers);

      // Make POST request to bank simulator
      ResponseEntity<BankResponse> response = restTemplate.exchange(
          bankUrl,
          HttpMethod.POST,
          entity,
          BankResponse.class
      );
      LOG.debug("processPayment:: ...finished calling bank");
      return response.getBody();
    } catch (ResourceAccessException e) {
      LOG.warn("processPayment:: Network error occurred, will retry: {}", e.getMessage());
      throw new BankProcessingException("Network error connecting to bank: " + e.getMessage());

    } catch (HttpServerErrorException e) {
      LOG.warn("processPayment:: Server error occurred ({}), will retry: {}",
          e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankProcessingException("Bank server error: " + e.getStatusCode());

    } catch (HttpClientErrorException e) {
      LOG.error("processPayment:: Client error ({}), not retrying: {}",
          e.getStatusCode(), e.getResponseBodyAsString());
      throw new BankProcessingException("Bank client error: " + e.getStatusCode());

    } catch (Exception e) {
      LOG.error("processPayment:: Unexpected error occurred: {}", e.getMessage());
      throw new BankProcessingException("Unexpected error during bank processing");
    }
  }

}
