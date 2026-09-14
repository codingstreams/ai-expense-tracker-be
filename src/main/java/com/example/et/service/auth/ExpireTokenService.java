package com.example.et.service.auth;

import java.time.Duration;

public interface ExpireTokenService {
  void addExpireToken(String jti);

  boolean isExpireToken(String jti);

  void addExpireToken(String jti, Duration ttl);
}
