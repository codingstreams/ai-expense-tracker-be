package com.example.et.controller.dto.auth;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, boolean onboarded,
                           String refreshToken) {
}