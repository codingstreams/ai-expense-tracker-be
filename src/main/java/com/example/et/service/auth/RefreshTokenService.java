package com.example.et.service.auth;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenService {
  void saveRefreshToken(String userId, String refreshTokenJti, Duration ttl);

  Optional<String> getRefreshToken(String userId);

  boolean isRefreshTokenValid(String userId, String jti);

  void deleteRefreshToken(String userId);

  boolean isPresent(String jti);
}
