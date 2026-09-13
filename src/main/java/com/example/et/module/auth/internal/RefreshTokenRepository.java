package com.example.et.module.auth.internal;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {
  void saveRefreshToken(String userId, String refreshToken, Duration ttl);

  Optional<String> getRefreshToken(String userId);

  boolean isRefreshTokenValid(String userId, String refreshToken);

  void deleteRefreshToken(String userId);
}
