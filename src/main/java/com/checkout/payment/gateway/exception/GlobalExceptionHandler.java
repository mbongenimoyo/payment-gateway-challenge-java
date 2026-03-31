package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.OffsetDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);


  @ExceptionHandler(InvalidPaymentException.class)
  public ResponseEntity<ErrorResponse> handleInvalidPayment(InvalidPaymentException ex) {
    LOG.warn("handleInvalidPayment:: handling invalid payment request");
    ErrorResponse error = new ErrorResponse(
        ex.getErrors().toString(),
        ex.getMessage(),
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BankProcessingException.class)
  public ResponseEntity<ErrorResponse> handleBankError(BankProcessingException ex) {
    LOG.error("handleBankError:: There was issue on the bank side :{}",ex.getMessage());
    ErrorResponse error = new ErrorResponse(
        "Payment processing failed",
        "Unable to process payment at this time",
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(PaymentNotFoundException ex) {
    LOG.warn("handleNotFound:: handling invalid payment request");
    ErrorResponse error = new ErrorResponse(
        "Payment not found",
        ex.getMessage(),
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    LOG.error("handleGeneric:: Unhandled error was thrown: {}",ex.getMessage());
    ErrorResponse error = new ErrorResponse(
        "Internal server error",
        "An unexpected error occurred",
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleJsonParse(HttpMessageNotReadableException ex) {
    LOG.warn("handleJsonParse:: Unhandled error was thrown");
    ErrorResponse error = new ErrorResponse(
        "Invalid request format",
        "The request body contains invalid JSON or malformed data",
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler( MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException( MethodArgumentNotValidException ex) {
    LOG.warn("handleMethodArgumentNotValidException:: handling invalid payment request");
    ErrorResponse error = new ErrorResponse(
        " MethodArgumentNotValidException",
        ex.getMessage(),
        PaymentStatus.REJECTED,
        OffsetDateTime.now()
    );
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }


}
