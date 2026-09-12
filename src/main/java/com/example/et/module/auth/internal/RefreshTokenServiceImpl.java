package com.example.et.module.auth.internal;

import com.example.et.module.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
  private static final String RT_PREFIX = "rt:";
  private final StringRedisTemplate redisTemplate;

  @Override
  public void saveRefreshToken(String userId, String refreshToken, Duration ttl) {
    redisTemplate.opsForValue().set(RT_PREFIX + userId, refreshToken, ttl);
    log.debug("Saved refresh token for user {} with TTL: {}s", userId, ttl.toSeconds());
  }

  @Override
  public Optional<String> getRefreshToken(String userId) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(RT_PREFIX + userId));
  }

  @Override
  public boolean isRefreshTokenValid(String userId, String refreshToken) {
    final String storedToken = redisTemplate.opsForValue().get(RT_PREFIX + userId);
    return storedToken != null && storedToken.equals(refreshToken);
  }

  @Override
  public void deleteRefreshToken(String userId) {
    redisTemplate.delete(RT_PREFIX + userId);
    log.debug("Deleted refresh token for user {}", userId);
  }
}
