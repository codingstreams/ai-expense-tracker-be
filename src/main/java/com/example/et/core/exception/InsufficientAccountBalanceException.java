package com.example.et.core.exception;

public class InsufficientAccountBalanceException extends ApiException {
  public InsufficientAccountBalanceException(Long accountId) {
    super(ErrorCode.INSUFFICIENT_BALANCE, String.format("Insufficient account balance for account %s", accountId));
  }
}
