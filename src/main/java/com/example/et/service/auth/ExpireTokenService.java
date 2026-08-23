package com.example.et.service.auth;

public interface ExpireTokenService {
  void addExpireToken(String token);
  boolean isExpireToken(String token);
}
