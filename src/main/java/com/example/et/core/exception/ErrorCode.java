package com.example.et.core.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  // ==========================================
  // Common / Generic
  // ==========================================
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request payload or parameters"),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resource not found"),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported"),

  // ==========================================
  // Auth & Security
  // ==========================================
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Full authentication is required to access this resource"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to access this resource"),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
  USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid or corrupted token"),
  TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Token has been revoked"),
  REFRESH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "Refresh token must not be blank"),

  // ==========================================
  // Account
  // ==========================================
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "Insufficient account balance"),
  CASH_ACCOUNT_IMMUTABLE(HttpStatus.BAD_REQUEST, "Cannot delete or alter cash account"),
  INVALID_BANK_IDS(HttpStatus.BAD_REQUEST, "Invalid bank IDs provided"),
  INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Amount must be greater than zero"),

  // ==========================================
  // Card
  // ==========================================
  CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "Card not found"),
  CARD_NOT_LINKED(HttpStatus.BAD_REQUEST, "Card is not linked to any account"),

  // ==========================================
  // Transaction
  // ==========================================
  TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Transaction not found"),
  INVALID_TRANSACTION_PAYLOAD(HttpStatus.BAD_REQUEST, "Either accountId or cardId must be provided"),
  INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "Invalid transaction type"),

  // ==========================================
  // Reference Data (Category / Payment Mode)
  // ==========================================
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),
  PAYMENT_MODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment mode not found"),

  // ==========================================
  // AI & Chat
  // ==========================================
  AI_CHAT_MESSAGE_BLANK(HttpStatus.BAD_REQUEST, "Chat message must not be blank"),
  AI_DETAILS_REQUIRED(HttpStatus.BAD_REQUEST, "Required transaction details cannot be null"),
  AI_WEEKLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Weekly AI insight limit reached (maximum 2 per week)"),
  AI_MONTHLY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Monthly AI insight limit reached (maximum 4 per month)");

  private final HttpStatus status;
  private final String defaultMessage;

  ErrorCode(HttpStatus status, String defaultMessage) {
    this.status = status;
    this.defaultMessage = defaultMessage;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public int getStatusCode() {
    return status.value();
  }

  public String getDefaultMessage() {
    return defaultMessage;
  }

  public String getCode() {
    return this.name();
  }
}
