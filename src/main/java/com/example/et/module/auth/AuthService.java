package com.example.et.module.auth;

import com.example.et.module.auth.dto.*;

public interface AuthService {
  AuthResponse register(CreateUserReq createUserReq);

  AuthResponse login(LoginReq loginReq);

  AuthResponse refreshToken(RefreshTokenReq refreshTokenReq);

  void logout(String token);

  void logout(String token, LogoutReq logoutReq);
}
