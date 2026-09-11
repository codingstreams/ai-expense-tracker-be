package com.example.et.service.auth;

import java.time.Duration;

public interface ExpireTokenService {
  void addExpireToken(String token);

  void addExpireToken(String token, Duration ttl);

  boolean isExpireToken(String token);
}
