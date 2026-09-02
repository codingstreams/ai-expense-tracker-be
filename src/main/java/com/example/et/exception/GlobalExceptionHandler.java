package com.example.et.exception;

import com.example.et.controller.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleException(RuntimeException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage(), LocalDateTime.now().toString()));
  }
}
