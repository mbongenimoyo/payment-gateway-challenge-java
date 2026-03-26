package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;

public class BankResponse implements Serializable {

  @JsonProperty("authorized")
  private final Boolean authorized;
  @JsonProperty("authorization_code")
  private final String authorizationCode;

  private final OffsetDateTime createAt;
  public BankResponse(Boolean authorized, String authorizationCode) {
    this.authorized = authorized;
    this.authorizationCode = authorizationCode;
    this.createAt =OffsetDateTime.now();
  }

  public Boolean getAuthorized() {
    return authorized;
  }

  public String getAuthorizationCode() {

    return authorizationCode;
  }
}
