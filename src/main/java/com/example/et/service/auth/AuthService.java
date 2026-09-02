package com.example.et.service.auth;

import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.LoginRequest;
import com.example.et.controller.dto.UserRegistrationRequest;

public interface AuthService {
  AuthResponse login(LoginRequest request);

  AuthResponse registerUser(UserRegistrationRequest request);

  void logout(String token);
}