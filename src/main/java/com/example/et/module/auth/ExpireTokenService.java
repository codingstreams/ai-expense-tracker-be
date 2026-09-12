package com.example.et.module.auth;

import java.time.Duration;

public interface ExpireTokenService {
  void addExpireToken(String token);

  void addExpireToken(String token, Duration ttl);

  boolean isExpireToken(String token);
}
