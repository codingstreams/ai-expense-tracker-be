package com.example.et.controller.dto;

public record ApiErrorResponse (int status, String message, String timestamp){
}
