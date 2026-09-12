package com.example.et.module.auth;

import com.example.et.module.auth.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> registerUser(@RequestBody CreateUserReq createUserReq) {
    log.info("Received user registration request for email: {}", createUserReq.email());

    final var authResponse = authService.register(createUserReq);

    log.info("Successfully registered user with email: {}", createUserReq.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginReq loginReq) {
    log.info("Received login request for user: {}", loginReq.email());

    final var authResponse = authService.login(loginReq);

    log.info("User logged in successfully: {}", loginReq.email());
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenReq refreshTokenReq) {
    log.info("Received token refresh request");

    final var authResponse = authService.refreshToken(refreshTokenReq);

    log.info("Token refreshed successfully");
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
      @RequestBody(required = false) LogoutReq logoutReq) {
    log.info("Received logout request");

    authService.logout(token, logoutReq);

    log.info("User logged out successfully");
    return ResponseEntity.noContent().build();
  }
}