package com.checkout.payment.gateway.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(PaymentGatewayController.class)
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @Test
  void getPostPaymentEventById_whenPaymentExists_thenReturnOkAndPaymentResponse() throws Exception {
    UUID paymentId = UUID.randomUUID();
    PaymentResponse response = new PaymentResponse(
        paymentId,
        PaymentStatus.AUTHORIZED,
        "1111",
        12,
        2030,
        "USD",
        1500L
    );
    when(paymentGatewayService.getPaymentById(paymentId)).thenReturn(response);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(paymentId.toString()))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("1111"))
        .andExpect(jsonPath("$.expiry_month").value(12))
        .andExpect(jsonPath("$.expiry_year").value(2030))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(1500));
  }

  @Test
  void getPostPaymentEventById_whenPaymentWithIdDoesNotExist_thenReturnNotFound() throws Exception {
    UUID paymentId = UUID.randomUUID();
    when(paymentGatewayService.getPaymentById(paymentId))
        .thenThrow(new PaymentNotFoundException(" not find payment", paymentId));

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + paymentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Payment not found"))
        .andExpect(jsonPath("$.message").value(" not find payment"))
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void processPayment_whenRequestIsValid_thenReturnOkAndPaymentResponse() throws Exception {
    UUID paymentId = UUID.randomUUID();
    PaymentResponse response = new PaymentResponse(
        paymentId,
        PaymentStatus.AUTHORIZED,
        "8887",
        10,
        2031,
        "EUR",
        2200L
    );
    when(paymentGatewayService.processPayment(any(), any())).thenReturn(response);

    String requestBody = """
        {
          "card_number": "2222405343248877",
          "expiry_month": 10,
          "expiry_year": 2031,
          "currency": "EUR",
          "amount": 2200,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(paymentId.toString()))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("8887"))
        .andExpect(jsonPath("$.expiry_month").value(10))
        .andExpect(jsonPath("$.expiry_year").value(2031))
        .andExpect(jsonPath("$.currency").value("EUR"))
        .andExpect(jsonPath("$.amount").value(2200));
  }

  @Test
  void processPayment_whenCardNumberIsMissing_thenReturnBadRequest() throws Exception {
    String requestBody = """
        {
          "expiry_month": 10,
          "expiry_year": 2031,
          "currency": "EUR",
          "amount": 2200,
          "cvv": "123"
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void processPayment_whenRequestBodyIsMalformedJson_thenReturnBadRequest() throws Exception {
    String malformedJson = "{ \"card_number\": ";

    mvc.perform(MockMvcRequestBuilders.post("/payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(malformedJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid request format"))
        .andExpect(jsonPath("$.status").value("Rejected"));
  }
}
