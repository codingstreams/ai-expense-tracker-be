package com.example.et.module.auth;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenService {
  void saveRefreshToken(String userId, String refreshToken, Duration ttl);

  Optional<String> getRefreshToken(String userId);

  boolean isRefreshTokenValid(String userId, String refreshToken);

  void deleteRefreshToken(String userId);
}
