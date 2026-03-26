package com.checkout.payment.gateway.util;

import java.util.Currency;
import java.util.Set;

public class CurrencyUtil {

  private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP");

  public static boolean isValidCurrency(String currencyCode){
    if(currencyCode==null || currencyCode.isEmpty()) return false;
    return SUPPORTED_CURRENCIES.contains(currencyCode.toUpperCase());
  }
}
