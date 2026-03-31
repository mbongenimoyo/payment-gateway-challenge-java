package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.exception.InvalidPaymentException;
import com.checkout.payment.gateway.model.api.CreatePaymentRequest;
import com.checkout.payment.gateway.model.domain.PaymentRequest;
import java.time.YearMonth;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentRequestTest {

  @Test
  public void fromDTO_whenDTOIsValid_ReturnPaymentRequestWithLastFourDigits() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);
    CreatePaymentRequest dto = new CreatePaymentRequest(
        "4111111111111111",
        nextMonth.getMonthValue(),
        nextMonth.getYear(),
        "USD",
        2000L,
        "123"
    );

    PaymentRequest paymentRequest = PaymentRequest.fromDTO(dto);

    Assertions.assertThat(paymentRequest.cardNumberLastFour()).isEqualTo("1111");
    Assertions.assertThat(paymentRequest.expiryMonth()).isEqualTo(nextMonth.getMonthValue());
    Assertions.assertThat(paymentRequest.expiryYear()).isEqualTo(nextMonth.getYear());
    Assertions.assertThat(paymentRequest.currency()).isEqualTo("USD");
    Assertions.assertThat(paymentRequest.amount()).isEqualTo(2000L);
    Assertions.assertThat(paymentRequest.cvv()).isEqualTo("123");
  }

  @Test
  public void constructor_whenCardNumberIsInvalid_ThrowInvalidPaymentException() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "abc",
            nextMonth.getMonthValue(),
            nextMonth.getYear(),
            "USD",
            1000L,
            "123"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Invalid Card Details"));
  }

  @Test
  public void constructor_whenCVVIsInvalid_ThrowInvalidPaymentException() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "4111111111111111",
            nextMonth.getMonthValue(),
            nextMonth.getYear(),
            "USD",
            1000L,
            "12"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Invalid Card Details"));
  }

  @Test
  public void constructor_whenExpiryMonthIsOutOfRange_ThrowInvalidPaymentException() {
    int nextYear = YearMonth.now().plusYears(1).getYear();

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "4111111111111111",
            13,
            nextYear,
            "USD",
            1000L,
            "123"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Invalid expiry month"));
  }

  @Test
  public void constructor_whenAmountIsZero_ThrowInvalidPaymentException() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "4111111111111111",
            nextMonth.getMonthValue(),
            nextMonth.getYear(),
            "USD",
            0L,
            "123"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Invalid amount"));
  }

  @Test
  public void constructor_whenCurrencyIsNotSupported_ThrowInvalidPaymentException() {
    YearMonth nextMonth = YearMonth.now().plusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "4111111111111111",
            nextMonth.getMonthValue(),
            nextMonth.getYear(),
            "KES",
            1000L,
            "123"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Invalid currency"));
  }

  @Test
  public void constructor_whenCardIsExpired_ThrowInvalidPaymentException() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "4111111111111111",
            lastMonth.getMonthValue(),
            lastMonth.getYear(),
            "USD",
            1000L,
            "123"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains("Card has expired"));
  }

  @Test
  public void constructor_whenMultipleFieldsAreInvalid_ThrowInvalidPaymentExceptionWithAllErrors() {
    YearMonth lastMonth = YearMonth.now().minusMonths(1);

    Assertions.assertThatThrownBy(() -> new PaymentRequest(
            "abc",
            0,
            lastMonth.getYear(),
            "KES",
            -10L,
            "1a"
        ))
        .isInstanceOf(InvalidPaymentException.class)
        .satisfies(exception -> Assertions.assertThat(((InvalidPaymentException) exception).getErrors())
            .contains(
                "Invalid Card Details",
                "Invalid expiry month",
                "Invalid amount",
                "Invalid currency",
                "Card has expired"
            ));
  }
}