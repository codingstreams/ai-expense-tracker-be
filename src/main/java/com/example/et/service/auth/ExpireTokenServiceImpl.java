package com.example.et.service.auth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExpireTokenServiceImpl implements ExpireTokenService {
  private final List<String> store = new CopyOnWriteArrayList<>();

  @Override
  public void addExpireToken(String jti) {
    if (!isExpireToken(jti)) {
      store.add(jti);
    }
  }

  @Override
  public boolean isExpireToken(String jti) {
    return store.contains(jti);
  }

  @Override
  public void addExpireToken(String jti, Duration ttl) {
    throw new UnsupportedOperationException();
  }
}
