package com.example.et.core.exception;

public record ApiErrorResponse(int status, String message, String timestamp) {
}
