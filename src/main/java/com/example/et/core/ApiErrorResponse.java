package com.example.et.core;

public record ApiErrorResponse(int status, String message, String timestamp) {
}
