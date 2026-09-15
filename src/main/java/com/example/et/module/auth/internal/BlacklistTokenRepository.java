package com.example.et.module.auth.internal;

import java.time.Duration;

public interface BlacklistTokenRepository {
  void add(String token);

  void add(String token, Duration ttl);

  boolean isExpire(String token);
}
