package com.checkout.payment.gateway.model.api;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;

public record ErrorResponse(@JsonProperty("error") String error,
                            @JsonProperty("message") String message,
                            @JsonProperty("status") PaymentStatus status,
                            @JsonProperty("timestamp") OffsetDateTime timestamp) implements
    Serializable {

}
