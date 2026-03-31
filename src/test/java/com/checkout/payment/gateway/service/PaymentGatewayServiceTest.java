package com.checkout.payment.gateway.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankProcessingException;
import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.api.PaymentResponse;
import com.checkout.payment.gateway.model.bank.BankResponse;
import com.checkout.payment.gateway.model.domain.PaymentRequest;
import com.checkout.payment.gateway.model.entity.Payment;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private BankClient bankClient;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private CreatePaymentRequest validRequest;
  private UUID paymentId;

  @BeforeEach
  void setUp() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);
    validRequest = new CreatePaymentRequest(
        "4111111111111111",
        nextMonth.getMonthValue(),
        nextMonth.getYear(),
        "USD",
        3000L,
        "123"
    );
    paymentId = UUID.randomUUID();
  }

  @Test
  public void getPaymentById_whenPaymentExists_ReturnPaymentResponse() {
    Payment payment = new Payment(PaymentRequest.fromDTO(validRequest), paymentId);
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(payment));

    PaymentResponse response = paymentGatewayService.getPaymentById(paymentId);

    Assertions.assertThat(response.id()).isEqualTo(paymentId);
    Assertions.assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    Assertions.assertThat(response.cardNumberLastFour()).isEqualTo("1111");
    verify(paymentsRepository, times(1)).get(paymentId);
  }

  @Test
  public void getPaymentById_whenPaymentDoesNotExist_ThrowPaymentNotFoundException() {
    UUID unknownPaymentId = UUID.randomUUID();
    when(paymentsRepository.get(unknownPaymentId)).thenReturn(Optional.empty());

    Assertions.assertThatThrownBy(() -> paymentGatewayService.getPaymentById(unknownPaymentId))
        .isInstanceOf(PaymentNotFoundException.class)
        .hasMessage("could not find payment");

    verify(paymentsRepository, times(1)).get(unknownPaymentId);
  }

  @Test
  public void processPayment_whenBankAuthorizesPayment_ReturnAuthorizedAndSavePayment() {
    when(bankClient.processPayment(validRequest)).thenReturn(new BankResponse(true, "AUTH-123"));

    PaymentResponse response = paymentGatewayService.processPayment(validRequest, paymentId);

    Assertions.assertThat(response.id()).isEqualTo(paymentId);
    Assertions.assertThat(response.status()).isEqualTo(PaymentStatus.AUTHORIZED);
    Assertions.assertThat(response.cardNumberLastFour()).isEqualTo("1111");
    verify(bankClient, times(1)).processPayment(validRequest);
    verify(paymentsRepository, times(1)).add(org.mockito.ArgumentMatchers.any(Payment.class));
  }

  @Test
  public void processPayment_whenBankDeclinesPayment_ReturnDeclinedAndSavePayment() {
    when(bankClient.processPayment(validRequest)).thenReturn(new BankResponse(false, ""));

    PaymentResponse response = paymentGatewayService.processPayment(validRequest, paymentId);

    Assertions.assertThat(response.id()).isEqualTo(paymentId);
    Assertions.assertThat(response.status()).isEqualTo(PaymentStatus.DECLINED);
    verify(bankClient, times(1)).processPayment(validRequest);
    verify(paymentsRepository, times(1)).add(org.mockito.ArgumentMatchers.any(Payment.class));
  }

  @Test
  public void processPayment_whenBankClientThrowsException_SaveRejectedPaymentAndRethrow() {
    BankProcessingException bankException = new BankProcessingException("bank unavailable");
    when(bankClient.processPayment(validRequest)).thenThrow(bankException);
    ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

    Assertions.assertThatThrownBy(() -> paymentGatewayService.processPayment(validRequest, paymentId))
        .isInstanceOf(BankProcessingException.class)
        .hasMessage("bank unavailable");

    verify(paymentsRepository, times(1)).add(paymentCaptor.capture());
    Payment savedPayment = paymentCaptor.getValue();
    Assertions.assertThat(savedPayment.toPaymentResponseDTO().status()).isEqualTo(PaymentStatus.REJECTED);
    verify(bankClient, times(1)).processPayment(validRequest);
  }

  @Test
  public void processPayment_whenBankReturnsNullResponse_ThrowRuntimeExceptionAndDoNotSavePayment() {
    when(bankClient.processPayment(validRequest)).thenReturn(null);

    Assertions.assertThatThrownBy(() -> paymentGatewayService.processPayment(validRequest, paymentId))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Could not process payment");

    verify(bankClient, times(1)).processPayment(validRequest);
    verify(paymentsRepository, never()).add(org.mockito.ArgumentMatchers.any(Payment.class));
  }

  @Test
  public void processPayment_whenRequestIsInvalid_ThrowInvalidPaymentException() {
    CreatePaymentRequest invalidRequest = new CreatePaymentRequest(
        "abc",
        1,
        2000,
        "USD",
        100L,
        "12"
    );

    Assertions.assertThatThrownBy(() -> paymentGatewayService.processPayment(invalidRequest, paymentId))
        .isInstanceOf(InvalidPaymentException.class);

    verify(bankClient, never()).processPayment(invalidRequest);
    verify(paymentsRepository, never()).add(org.mockito.ArgumentMatchers.any(Payment.class));
  }
}