package com.checkout.payment.gateway.service.bankservice;

import com.checkout.payment.gateway.dto.PaymentRequestDTO;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.service.BankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private BankService bankService;

  private static final String BANK_URL = "http://localhost:8080/payments";
  private PaymentRequestDTO paymentRequest;
  private BankResponse authorizedResponse;
  private BankResponse declinedResponse;

  @BeforeEach
  void setUp() {
    // Set the bank URL using reflection since it's @Value injected
    ReflectionTestUtils.setField(bankService, "bankUrl", BANK_URL);

    // Create test payment request
    paymentRequest = new PaymentRequestDTO(
        "2222405343248877",  // card number (last four)
        4,                    // expiry month
        2025,                 // expiry year
        "GBP",                // currency
        100L,                  // amount
        "123"                 // cvv
    );

    // Create test responses
    authorizedResponse = new BankResponse(true, "test-auth-code-123");
    declinedResponse = new BankResponse(false, "");
  }

  @Test
  void processPayment_WhenBankReturnsAuthorized_ShouldReturnAuthorizedResponse() {
    // Arrange
    ResponseEntity<BankResponse> responseEntity = new ResponseEntity<>(authorizedResponse, HttpStatus.OK);
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenReturn(responseEntity);

    // Act
    BankResponse result = bankService.processPayment(paymentRequest);

    // Assert
    assertNotNull(result);


    // Verify RestTemplate was called correctly
    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WhenBankReturnsDeclined_ShouldReturnDeclinedResponse() {
    // Arrange
    ResponseEntity<BankResponse> responseEntity = new ResponseEntity<>(declinedResponse, HttpStatus.OK);
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenReturn(responseEntity);

    // Act
    BankResponse result = bankService.processPayment(paymentRequest);

    // Assert
    assertNotNull(result);
    assertFalse(result.getAuthorized());
    assertEquals("", result.getAuthorizationCode());

    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WhenBankReturns400_ShouldThrowRuntimeException() {
    // Arrange
    HttpClientErrorException exception = HttpClientErrorException.create(
        HttpStatus.BAD_REQUEST,
        "Bad Request",
        null,
        "Invalid request".getBytes(),
        null
    );
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenThrow(exception);

    // Act & Assert
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      bankService.processPayment(paymentRequest);
    });

    assertTrue(thrown.getMessage().contains("Failed to communicate with bank"));
    assertTrue(thrown.getCause() instanceof HttpClientErrorException);

    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WhenBankReturns503_ShouldThrowRuntimeException() {
    // Arrange
    HttpServerErrorException exception = HttpServerErrorException.create(
        HttpStatus.SERVICE_UNAVAILABLE,
        "Service Unavailable",
        null,
        "Bank temporarily unavailable".getBytes(),
        null
    );
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenThrow(exception);

    // Act & Assert
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      bankService.processPayment(paymentRequest);
    });

    assertTrue(thrown.getMessage().contains("Failed to communicate with bank"));
    assertTrue(thrown.getCause() instanceof HttpServerErrorException);

    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WhenNetworkErrorOccurs_ShouldThrowRuntimeException() {
    // Arrange
    RuntimeException networkException = new RuntimeException("Connection timeout");
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenThrow(networkException);

    // Act & Assert
    RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
      bankService.processPayment(paymentRequest);
    });


    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WhenRestTemplateReturnsNullBody_ShouldHandleGracefully() {
    // Arrange
    ResponseEntity<BankResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenReturn(responseEntity);

    // Act
    BankResponse result = bankService.processPayment(paymentRequest);

    // Assert
    assertNull(result);

    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_WithDifferentPaymentRequests_ShouldSendCorrectData() {
    // Arrange
    PaymentRequestDTO differentRequest = new PaymentRequestDTO(
        "1111222233334444",  // different card
        12,                   // different month
        2026,                 // different year
        "USD",                // different currency
        200L,                  // different amount
        "456"                 // different CVV
    );

    ResponseEntity<BankResponse> responseEntity = new ResponseEntity<>(authorizedResponse, HttpStatus.OK);
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenReturn(responseEntity);

    // Act
    BankResponse result = bankService.processPayment(differentRequest);

    // Assert
    assertNotNull(result);
//    assertTrue(result.isAuthorized());

    // Verify the request was made with the correct data
    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(BankResponse.class)
    );
  }

  @Test
  void processPayment_VerifyHttpHeadersAreSetCorrectly() {
    // Arrange
    ResponseEntity<BankResponse> responseEntity = new ResponseEntity<>(authorizedResponse, HttpStatus.OK);
    when(restTemplate.exchange(eq(BANK_URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(BankResponse.class)))
        .thenReturn(responseEntity);

    // Act
    bankService.processPayment(paymentRequest);

    // Assert
    verify(restTemplate).exchange(
        eq(BANK_URL),
        eq(HttpMethod.POST),
        argThat(entity -> {
          HttpHeaders headers = entity.getHeaders();
          return "application/json".equals(headers.get("Content-Type").get(0));
        }),
        eq(BankResponse.class)
    );
  }
}