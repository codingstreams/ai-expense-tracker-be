package com.example.et.service.exception;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(String description) {
    super(description);
  }
}
