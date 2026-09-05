package com.example.et.controller.dto.error;

public record ApiErrorResponse (int status, String message, String timestamp){
}
