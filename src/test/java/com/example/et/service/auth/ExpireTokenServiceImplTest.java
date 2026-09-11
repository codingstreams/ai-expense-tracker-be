package com.example.et.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpireTokenServiceImplTest {

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  private ExpireTokenServiceImpl expireTokenService;

  @BeforeEach
  void setUp() {
    expireTokenService = new ExpireTokenServiceImpl(redisTemplate);
  }

  @Test
  void addExpireToken_withDefaultTtl() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    expireTokenService.addExpireToken("sample-token");

    verify(valueOperations).set(eq("bl:at:sample-token"), eq("revoked"), eq(Duration.ofMinutes(15)));
  }

  @Test
  void addExpireToken_withCustomTtl() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Duration customTtl = Duration.ofMinutes(5);
    expireTokenService.addExpireToken("sample-token", customTtl);

    verify(valueOperations).set(eq("bl:at:sample-token"), eq("revoked"), eq(customTtl));
  }

  @Test
  void isExpireToken_whenTokenExistsInRedis_returnsTrue() {
    when(redisTemplate.hasKey("bl:at:sample-token")).thenReturn(true);

    boolean result = expireTokenService.isExpireToken("sample-token");

    assertTrue(result);
  }

  @Test
  void isExpireToken_whenTokenNotInRedis_returnsFalse() {
    when(redisTemplate.hasKey("bl:at:sample-token")).thenReturn(false);

    boolean result = expireTokenService.isExpireToken("sample-token");

    assertFalse(result);
  }
}
