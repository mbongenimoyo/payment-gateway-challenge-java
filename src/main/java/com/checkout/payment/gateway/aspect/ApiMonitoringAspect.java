// src/main/java/com/checkout/payment/gateway/aspect/ApiMonitoringAspect.java
package com.checkout.payment.gateway.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ApiMonitoringAspect {

  private static final Logger LOG = LoggerFactory.getLogger(ApiMonitoringAspect.class);

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Around("execution(* com.checkout.payment.gateway.controller.PaymentGatewayController.*(..))")
  public Object monitorApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();
    String methodName = joinPoint.getSignature().getName();
    String className = joinPoint.getTarget().getClass().getSimpleName();

    Map<String, Object> apiEvent = new HashMap<>();
    apiEvent.put("className", className);
    apiEvent.put("methodName", methodName);
    apiEvent.put("timestamp", LocalDateTime.now().toString());
    apiEvent.put("startTime", startTime);

    try {
      // Proceed with the method execution
      Object result = joinPoint.proceed();

      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      apiEvent.put("status", "SUCCESS");
      apiEvent.put("duration", duration);
      apiEvent.put("endTime", endTime);

      // Sanitize request parameters - exclude sensitive data
      Object[] args = joinPoint.getArgs();
      Map<String, Object> sanitizedParams = sanitizeRequestData(methodName, args);
      apiEvent.put("requestParams", sanitizedParams);

      // Add response info (but don't log sensitive data)
      apiEvent.put("responseType", result != null ? result.getClass().getSimpleName() : "null");

      LOG.info("API call completed: {}.{} - Duration: {}ms", className, methodName, duration);

      return result;

    } catch (Exception e) {
      long endTime = System.currentTimeMillis();
      long duration = endTime - startTime;

      apiEvent.put("status", "ERROR");
      apiEvent.put("duration", duration);
      apiEvent.put("endTime", endTime);
      apiEvent.put("error", e.getMessage());

      LOG.error("API call failed: {}.{} - Duration: {}ms - Error: {}",
          className, methodName, duration, e.getMessage());

      throw e;
    } finally {
      // Send to Kafka
      try {
        String eventJson = objectMapper.writeValueAsString(apiEvent);
        kafkaTemplate.send("api-monitoring", eventJson);
        LOG.debug("Sent API monitoring event to Kafka: {}", eventJson);
      } catch (Exception e) {
        LOG.error("Failed to send API monitoring event to Kafka: {}", e.getMessage());
      }
    }
  }

  /**
   * Sanitize request data to remove sensitive information
   */
  private Map<String, Object> sanitizeRequestData(String methodName, Object[] args) {
    Map<String, Object> sanitized = new HashMap<>();

    for (int i = 0; i < args.length; i++) {
      Object arg = args[i];

      if (arg != null) {
        if ("processPayment".equals(methodName) && arg.getClass().getSimpleName().contains("PaymentRequestDTO")) {
          // For payment requests, only include non-sensitive fields
          Map<String, Object> paymentInfo = new HashMap<>();
          paymentInfo.put("currency", getFieldValue(arg, "currency"));
          paymentInfo.put("amount", getFieldValue(arg, "amount"));
          paymentInfo.put("expiryMonth", getFieldValue(arg, "expiryMonth"));
          paymentInfo.put("expiryYear", getFieldValue(arg, "expiryYear"));
          // DELIBERATELY exclude: cardNumber, cvv
          sanitized.put("paymentRequest", paymentInfo);
        } else {
          // For other requests, include the parameter type but not the value
          sanitized.put("param" + i, arg.getClass().getSimpleName());
        }
      }
    }

    return sanitized;
  }

  /**
   * Safely extract field value using reflection
   */
  private Object getFieldValue(Object obj, String fieldName) {
    try {
      java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.get(obj);
    } catch (Exception e) {
      LOG.warn("Could not extract field {}: {}", fieldName, e.getMessage());
      return "REDACTED";
    }
  }
}