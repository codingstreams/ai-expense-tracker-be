package com.example.et.module.auth.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistTokenRepositoryImpl implements BlacklistTokenRepository {
  private static final String BLACKLIST_PREFIX = "bl:at:";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

  private final StringRedisTemplate redisTemplate;

  @Override
  public void add(String token) {
    add(token, DEFAULT_TTL);
  }

  @Override
  public void add(String token, Duration ttl) {
    redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "revoked", ttl);
    log.debug("Token blacklisted with TTL: {}s", ttl.toSeconds());
  }

  @Override
  public boolean isExpire(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
  }
}
