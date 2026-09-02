package com.example.et.service.auth;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExpireTokenServiceImpl implements ExpireTokenService {
  private final List<String> expireTokens = new CopyOnWriteArrayList<>();

  @Override
  public void addExpireToken(String token) {
    if (!isExpireToken(token)) {
      this.expireTokens.add(token);
    }
  }

  @Override
  public boolean isExpireToken(String token) {
    return this.expireTokens.contains(token);
  }
}
