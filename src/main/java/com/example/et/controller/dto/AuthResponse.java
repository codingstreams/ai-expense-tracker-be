package com.example.et.controller.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, boolean onboarded) {
}