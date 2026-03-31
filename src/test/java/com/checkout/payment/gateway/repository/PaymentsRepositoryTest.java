package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.model.domain.PaymentRequest;
import com.checkout.payment.gateway.model.entity.Payment;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentsRepositoryTest {

  @Test
  public void add_whenPaymentExists_thenGetReturnsPayment() {
    PaymentsRepository repository = new PaymentsRepository();
    Payment payment = new Payment(validPaymentRequest(1500L), UUID.randomUUID());

    repository.add(payment);

    Optional<Payment> result = repository.get(payment.getId());
    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result.get()).isSameAs(payment);
  }

  @Test
  public void get_whenPaymentDoesNotExist_thenReturnEmptyOptional() {
    PaymentsRepository repository = new PaymentsRepository();

    Optional<Payment> result = repository.get(UUID.randomUUID());

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  public void add_whenPaymentIdAlreadyExists_thenOverwriteWithLatestPayment() {
    PaymentsRepository repository = new PaymentsRepository();
    UUID paymentId = UUID.randomUUID();
    Payment firstPayment = new Payment(validPaymentRequest(1000L), paymentId);
    Payment latestPayment = new Payment(validPaymentRequest(3000L), paymentId);

    repository.add(firstPayment);
    repository.add(latestPayment);

    Optional<Payment> result = repository.get(paymentId);
    Assertions.assertThat(result).isPresent();
    Assertions.assertThat(result.get()).isSameAs(latestPayment);
  }

  private PaymentRequest validPaymentRequest(long amount) {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);
    return new PaymentRequest(
        "4111111111111111",
        nextMonth.getMonthValue(),
        nextMonth.getYear(),
        "USD",
        amount,
        "123"
    );
  }
}