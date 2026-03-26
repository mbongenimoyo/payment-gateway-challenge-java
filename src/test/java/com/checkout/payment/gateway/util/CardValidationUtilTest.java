package com.checkout.payment.gateway.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CardValidationUtilTest {


  //whenCardNumberIsNull_ReturnFalse
  @Test
  public void isValidCardNumber_whenCardNumberIsNull_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber(null))
        .isFalse();
  }

  //whenCardNumberIsEmpty_ReturnFalse
  @Test
  public void isValidCardNumber_whenCardNumberIsEmpty_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber(""))
        .isFalse();
  }

  //whenCardNumberIsValid_ReturnTrue
  @Test
  public void isValidCardNumber_whenCardNumberIsValid_ReturnTrue() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber("2222405343248877"))
        .isTrue();
  }

  //whenCardNumberContainsInvalidCharacters_ReturnFalse
  @Test
  public void isValidCardNumber_whenCardNumberContainsNonNumericCharacters_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber("££2222405343248877"))
        .isFalse();
  }

  //whenCardNumberIsTooShort_ReturnFalse
  @Test
  public void isValidCardNumber_whenCardNumberIsTooShort_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber("123456789"))
        .isFalse();
  }
  //whenCardNumberIsTooLong_ReturnFalse
  @Test
  public void isValidCardNumber_whenCardNumberIsTooLong_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber("22224053432488771234556"))
        .isFalse();
  }
  //whenCardNumberIsNegative_ReturnFalse

  @Test
  public void isValidCardNumber_whenCardNumberIsNegative_ReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCardNumber("-22224053432488771234556"))
        .isFalse();
  }

  //whenCVVIsValid_thenReturnTrue
  @Test
  public void isValidCVV_whenCVVIsValid_thenReturnTrue() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV("1234"))
        .isTrue();
  }
  //whenCVVContainsNonNumericCharacters_thenReturnFalse

  @Test
  public void isValidCVV_whenCVVContainsNonNumericCharacters_thenReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV("1£34"))
        .isFalse();
  }
  //whenCVVContainsIsLongerThan4Characters_thenReturnFalse
  @Test
  public void isValidCVV_whenCVVContainsIsLongerThan4Characters_thenReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV("12345"))
        .isFalse();
  }

  //whenCVVContainsIsShorterThan3Characters_thenReturnFalse

  @Test
  public void isValidCVV_whenCVVContainsIsShorterThan3Characters_thenReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV("12"))
        .isFalse();
  }

  @Test
  public void isValidCVV_whenCVVIsNull_thenReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV(null))
        .isFalse();
  }

  @Test
  public void isValidCVV_whenCVVIsEmpty_thenReturnFalse() {
    Assertions
        .assertThat(CardValidationUtil.isValidCVV(""))
        .isFalse();
  }
}