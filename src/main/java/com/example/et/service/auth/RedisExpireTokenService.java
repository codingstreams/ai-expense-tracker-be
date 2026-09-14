package com.example.et.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service("redisExpireTokenService")
@RequiredArgsConstructor
@Slf4j
public class RedisExpireTokenService implements ExpireTokenService {
  private static final String BLACKLIST_PREFIX = "bl:at:";
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

  private final StringRedisTemplate redisTemplate;

  @Override
  public void addExpireToken(String jti) {
    addExpireToken(jti, DEFAULT_TTL);
  }

  @Override
  public boolean isExpireToken(String jti) {
    return redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
  }

  @Override
  public void addExpireToken(String jti, Duration ttl) {
    redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "revoked", ttl.toMillis());
    log.debug("Token blacklisted with TTL: {}s", ttl.toSeconds());
  }
}
