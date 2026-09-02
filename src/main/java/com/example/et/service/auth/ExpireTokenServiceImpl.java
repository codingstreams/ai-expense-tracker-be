package com.example.et.service.auth;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ExpireTokenServiceImpl implements ExpireTokenService{
  private final List<String> store = new CopyOnWriteArrayList<>();
  @Override
  public void addExpireToken(String token) {
    if(!isExpireToken(token)){
      store.add(token);
    }
  }

  @Override
  public boolean isExpireToken(String token) {
    return store.contains(token);
  }
}
