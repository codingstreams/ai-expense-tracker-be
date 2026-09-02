package com.example.et.controller;

import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.LoginRequest;
import com.example.et.controller.dto.UserRegistrationRequest;
import com.example.et.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> registerUser(@RequestBody UserRegistrationRequest userRegistrationRequest) {
    final var authResponse = authService.registerUser(userRegistrationRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    final var authResponse = authService.login(loginRequest);
    return ResponseEntity.status(HttpStatus.OK).body(authResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("AUTHORIZATION") String token) {
    authService.logout(token);
    return ResponseEntity.ok().build();
  }
}
