package com.example.et.module.auth.dto;

import java.time.Instant;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expireTime,
    long expiresInSeconds,
    boolean onboarded
) {
  public AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds, boolean onboarded) {
    this(accessToken, refreshToken, tokenType, Instant.now().plusSeconds(expiresInSeconds).toEpochMilli(), expiresInSeconds, onboarded);
  }
}