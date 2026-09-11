package com.example.et.service.auth;

import com.example.et.controller.dto.auth.*;

public interface AuthService {
  AuthResponse register(CreateUserReq createUserReq);

  AuthResponse login(LoginReq loginReq);

  AuthResponse refreshToken(RefreshTokenReq refreshTokenReq);

  void logout(String token);

  void logout(String token, LogoutReq logoutReq);
}
