package com.example.et.module.auth;

import com.example.et.module.auth.dto.*;

public interface AuthService {
  AuthSuccessResponse register(RegisterUserRequest registerUserRequest);

  AuthSuccessResponse login(LoginRequest loginRequest);

  AuthSuccessResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

  void logout(String token, LogoutRequest logoutRequest);
}
