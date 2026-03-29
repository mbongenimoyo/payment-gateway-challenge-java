package com.checkout.payment.gateway.util;

import java.time.YearMonth;

public class CardValidationUtil {


  public static final String CVV_REGEX = "\\d{3,4}";

  public static boolean isValidCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.trim().isEmpty()) {
      return false;
    }
    if (!cardNumber.chars().allMatch(Character::isDigit)) {
      return false;
    }
    if (cardNumber.length() < 14 || cardNumber.length() > 19) {
      return false;
    }
    return true;
  }
  public static boolean isValidCVV(String cvv){
    if (cvv == null || cvv.isEmpty()) return false;
    return cvv.matches(CVV_REGEX);
  }


  public static boolean isValidExpiryDate(int expiryMonth, int expiryYear) {
    try{
      YearMonth current = YearMonth.now();
      YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
      return expiry.isAfter(current);
    } catch (Exception e) {
      return false;
    }

}
}
