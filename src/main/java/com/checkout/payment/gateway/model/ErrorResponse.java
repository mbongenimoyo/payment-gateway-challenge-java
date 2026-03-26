package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record ErrorResponse(@JsonProperty("error") String error,
                            @JsonProperty("message") String message,
                            @JsonProperty("timestamp") OffsetDateTime timestamp) {

}
