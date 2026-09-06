package com.example.et.service.auth;

import com.example.et.controller.dto.auth.AuthResponse;
import com.example.et.controller.dto.auth.LoginReq;
import com.example.et.controller.dto.auth.CreateUserReq;

public interface AuthService {
  AuthResponse register(CreateUserReq createUserReq);

  AuthResponse login(LoginReq loginReq);

  void logout(String token);
}
