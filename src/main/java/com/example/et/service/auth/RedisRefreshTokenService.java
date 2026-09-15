package com.example.et.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRefreshTokenService implements RefreshTokenService {
  private static final String RT_PREFIX = "rt:";
  private final StringRedisTemplate redisTemplate;

  @Override
  public void saveRefreshToken(String userId, String refreshTokenJti, Duration ttl) {
    redisTemplate.opsForValue().set(RT_PREFIX + refreshTokenJti, userId, ttl);
    log.debug("Saved refresh token for user {} with TTL: {}s", userId, ttl.toSeconds());
  }

  @Override
  public Optional<String> getRefreshToken(String userId) {
    return Optional.empty();
  }

  @Override
  public boolean isRefreshTokenValid(String userId, String jti) {
    final String storedTokenUid = redisTemplate.opsForValue().get(RT_PREFIX + jti);
    return storedTokenUid != null && storedTokenUid.equals(userId);
  }

  @Override
  public void deleteRefreshToken(String jti) {
    redisTemplate.delete(RT_PREFIX + jti);
  }

  @Override
  public boolean isPresent(String jti) {
    return redisTemplate.hasKey(RT_PREFIX + jti);
  }
}
