package com.example.et.core;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path,
    String timestamp,
    Map<String, String> fieldErrors
) {

  // 3-arg constructor for backward compatibility (e.g., CustomAuthEntryPoint)
  public ApiErrorResponse(int status, String message, String timestamp) {
    this(status, null, message, null, timestamp, null);
  }

  // 4-arg constructor
  public ApiErrorResponse(int status, String error, String message, String timestamp) {
    this(status, error, message, null, timestamp, null);
  }

  public static ApiErrorResponse of(int status, String error, String message, String path) {
    return new ApiErrorResponse(status, error, message, path, Instant.now().toString(), null);
  }

  public static ApiErrorResponse of(int status, String message, String path) {
    return new ApiErrorResponse(status, null, message, path, Instant.now().toString(), null);
  }

  public static ApiErrorResponse ofValidation(String path, Map<String, String> fieldErrors) {
    return new ApiErrorResponse(400, "VALIDATION_FAILED", "Validation failed for one or more fields", path, Instant.now().toString(), fieldErrors);
  }
}
