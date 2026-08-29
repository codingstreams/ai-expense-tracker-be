package com.example.et.service.auth;

import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.UserRegistrationRequest;

public interface AuthService {
  AuthResponse register(UserRegistrationRequest userRegistrationRequest);
}
