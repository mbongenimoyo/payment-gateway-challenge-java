package com.checkout.payment.gateway.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyUtilTest {

  @ParameterizedTest
  @ValueSource(strings = {"USD", "EUR", "GBP", "usd", "eur", "gbp"})  // Test case sensitivity
  void isValidCurrency_withSupportedCurrencies_returnsTrue(String currencyCode) {
    assertTrue(CurrencyUtil.isValidCurrency(currencyCode));
  }

  @ParameterizedTest
  @ValueSource(strings = {"@@@","AaB", "INVALID", "", "123", "AB", "US", "USDD"})
  void isValidCurrency_withUnsupportedCurrencies_returnsFalse(String currencyCode) {
    assertFalse(CurrencyUtil.isValidCurrency(currencyCode));
  }

}