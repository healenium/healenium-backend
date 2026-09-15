package com.epam.healenium.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionExpiredException.class)
    public ResponseEntity<Map<String, String>> handleSubscriptionExpired(SubscriptionExpiredException ex) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of(
                        "error", "subscription_expired",
                        "message", ex.getMessage()
                ));
    }
}
