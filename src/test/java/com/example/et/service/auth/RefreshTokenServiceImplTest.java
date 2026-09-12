package com.example.et.service.auth;

import com.example.et.module.auth.internal.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private RefreshTokenServiceImpl refreshTokenService;

  @BeforeEach
  void setUp() {
    refreshTokenService = new RefreshTokenServiceImpl(redisTemplate);
  }

  @Test
  void saveRefreshToken_storesWithTtl() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    refreshTokenService.saveRefreshToken("user-123", "rt-xyz", Duration.ofDays(7));

    verify(valueOperations).set("rt:user-123", "rt-xyz", Duration.ofDays(7));
  }

  @Test
  void getRefreshToken_whenPresent_returnsToken() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("rt:user-123")).thenReturn("rt-xyz");

    Optional<String> token = refreshTokenService.getRefreshToken("user-123");

    assertTrue(token.isPresent());
    assertEquals("rt-xyz", token.get());
  }

  @Test
  void isRefreshTokenValid_matchingToken_returnsTrue() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("rt:user-123")).thenReturn("rt-xyz");

    boolean isValid = refreshTokenService.isRefreshTokenValid("user-123", "rt-xyz");

    assertTrue(isValid);
  }

  @Test
  void isRefreshTokenValid_differentToken_returnsFalse() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("rt:user-123")).thenReturn("rt-other");

    boolean isValid = refreshTokenService.isRefreshTokenValid("user-123", "rt-xyz");

    assertFalse(isValid);
  }

  @Test
  void deleteRefreshToken_removesKey() {
    refreshTokenService.deleteRefreshToken("user-123");

    verify(redisTemplate).delete("rt:user-123");
  }
}
