package com.example.et.module.auth.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProps {
  private String secretKey;
  private long expirationTimeAccessTokenInMinutes;
  private long expirationTimeRefreshTokenInDays = 7;
  private long latencyBufferInSeconds = 60;

  public long getExpirationTimeAccessTokenInSeconds() {
    return expirationTimeAccessTokenInMinutes * 60;
  }

  public long getExpirationTimeRefreshTokenInSeconds() {
    return expirationTimeRefreshTokenInDays * 24 * 60 * 60;
  }

  public long getAdjustedExpirationTimeAccessTokenInSeconds() {
    return Math.max(1, getExpirationTimeAccessTokenInSeconds() - latencyBufferInSeconds);
  }
}
