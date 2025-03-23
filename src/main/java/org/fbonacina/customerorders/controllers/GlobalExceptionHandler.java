package org.fbonacina.customerorders.controllers;

import org.fbonacina.customerorders.exceptions.OrderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<String> handleOptimisticLockingFailure(
      ObjectOptimisticLockingFailureException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body("Optimistic locking failure: " + ex.getMessage());
  }

  @ExceptionHandler(OrderException.class)
  public ResponseEntity<String> handleOrderCreationFailure(OrderException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Error on creating a new order: " + ex.getMessage());
  }
}
