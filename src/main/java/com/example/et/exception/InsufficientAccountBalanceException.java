package com.example.et.exception;

public class InsufficientAccountBalanceException extends RuntimeException {
  public InsufficientAccountBalanceException(Long accountId) {
    super(String.format("Insufficient account balance for account %s", accountId));
  }
}
