package com.example.et.core.exception;

import com.example.et.core.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(ApiException e, HttpServletRequest request) {
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ApiErrorResponse.of(
            errorCode.getStatusCode(),
            errorCode.getCode(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiErrorResponse.ofValidation(request.getRequestURI(), fieldErrors));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ErrorCode.UNAUTHORIZED.getCode(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ErrorCode.BAD_REQUEST.getCode(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            e.getMessage(),
            request.getRequestURI()
        ));
  }
}
