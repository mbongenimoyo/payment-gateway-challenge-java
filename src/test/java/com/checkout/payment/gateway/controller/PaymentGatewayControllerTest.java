package com.checkout.payment.gateway.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;


  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    UUID id = UUID.randomUUID();
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("could not find payment"));
  }

  //when valid payment request with even card number is made then return valid response with decline
  //when valid payment request with oden card number is made then return valid response with authorised
  //when valid payment request end with 0  card number is made then return valid response with decline
  //when valid payment request with missing card number  is made then return valid response with decline
  //when Payment request with missing cardnumber

}
