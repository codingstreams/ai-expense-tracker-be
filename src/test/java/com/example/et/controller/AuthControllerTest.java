package com.example.et.controller;

import com.example.et.module.auth.AuthController;
import com.example.et.module.auth.AuthService;
import com.example.et.module.auth.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService);
  }

  @Test
  void registerUser_returnsCreated() {
    RegisterUserRequest req = new RegisterUserRequest("Test", "test@example.com", "password");
    AuthSuccessResponse mockResponse = new AuthSuccessResponse("at", "rt", "Bearer", 600, false);
    when(authService.register(req)).thenReturn(mockResponse);

    ResponseEntity<AuthSuccessResponse> response = authController.registerUser(req);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("at", response.getBody().accessToken());
    assertEquals("rt", response.getBody().refreshToken());
  }

  @Test
  void login_returnsOk() {
    LoginRequest req = new LoginRequest("test@example.com", "password");
    AuthSuccessResponse mockResponse = new AuthSuccessResponse("at", "rt", "Bearer", 600, true);
    when(authService.login(req)).thenReturn(mockResponse);

    ResponseEntity<AuthSuccessResponse> response = authController.login(req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("at", response.getBody().accessToken());
  }

  @Test
  void refresh_returnsOk() {
    RefreshTokenRequest req = new RefreshTokenRequest("valid-rt");
    AuthSuccessResponse mockResponse = new AuthSuccessResponse("new-at", "new-rt", "Bearer", 600, true);
    when(authService.refreshToken(req)).thenReturn(mockResponse);

    ResponseEntity<AuthSuccessResponse> response = authController.refresh(req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assert response.getBody() != null;
    assertEquals("new-at", response.getBody().accessToken());
    assertEquals("new-rt", response.getBody().refreshToken());
  }

  @Test
  void logout_returnsNoContent() {
    LogoutRequest req = new LogoutRequest("valid-rt");

    ResponseEntity<Void> response = authController.logout("Bearer token-xyz", req);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(authService).logout("Bearer token-xyz", req);
  }
}
