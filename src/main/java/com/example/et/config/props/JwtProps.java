package com.example.et.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProps {
  private String secretKey;
  private long expirationTimeAccessTokenInMinutes;

  public long getExpirationTimeAccessTokenInSeconds() {
    return expirationTimeAccessTokenInMinutes * 60;
  }
}
