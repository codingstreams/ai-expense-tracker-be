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
  public ResponseEntity<AuthSuccessResponse> registerUser(@RequestBody RegisterUserRequest registerUserRequest) {
    log.info("Received user registration request for email: {}", registerUserRequest.email());

    final var authResponse = authService.register(registerUserRequest);

    log.info("Successfully registered user with email: {}", registerUserRequest.email());
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthSuccessResponse> login(@RequestBody LoginRequest loginRequest) {
    log.info("Received login request for user: {}", loginRequest.email());

    final var authResponse = authService.login(loginRequest);

    log.info("User logged in successfully: {}", loginRequest.email());
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthSuccessResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
    log.info("Received token refresh request");

    final var authResponse = authService.refreshToken(refreshTokenRequest);

    log.info("Token refreshed successfully");
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
      @RequestBody(required = false) LogoutRequest logoutRequest) {
    log.info("Received logout request");

    authService.logout(token, logoutRequest);

    log.info("User logged out successfully");
    return ResponseEntity.noContent().build();
  }
}