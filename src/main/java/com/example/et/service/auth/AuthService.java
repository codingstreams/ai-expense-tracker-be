package com.example.et.service.auth;

import com.example.et.controller.dto.auth.AuthResponse;
import com.example.et.controller.dto.auth.LoginRequest;
import com.example.et.controller.dto.auth.UserRegistrationRequest;

public interface AuthService {
  AuthResponse register(UserRegistrationRequest userRegistrationRequest);

  AuthResponse login(LoginRequest loginRequest);

  void logout(String token);
}
