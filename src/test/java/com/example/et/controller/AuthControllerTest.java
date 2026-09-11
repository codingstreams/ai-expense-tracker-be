package com.example.et.controller;

import com.example.et.controller.dto.auth.*;
import com.example.et.service.auth.AuthService;
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
    CreateUserReq req = new CreateUserReq("Test", "test@example.com", "password");
    AuthResponse mockResponse = new AuthResponse("at", "rt", "Bearer", 600, false);
    when(authService.register(req)).thenReturn(mockResponse);

    ResponseEntity<AuthResponse> response = authController.registerUser(req);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("at", response.getBody().accessToken());
    assertEquals("rt", response.getBody().refreshToken());
  }

  @Test
  void login_returnsOk() {
    LoginReq req = new LoginReq("test@example.com", "password");
    AuthResponse mockResponse = new AuthResponse("at", "rt", "Bearer", 600, true);
    when(authService.login(req)).thenReturn(mockResponse);

    ResponseEntity<AuthResponse> response = authController.login(req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("at", response.getBody().accessToken());
  }

  @Test
  void refresh_returnsOk() {
    RefreshTokenReq req = new RefreshTokenReq("valid-rt");
    AuthResponse mockResponse = new AuthResponse("new-at", "new-rt", "Bearer", 600, true);
    when(authService.refreshToken(req)).thenReturn(mockResponse);

    ResponseEntity<AuthResponse> response = authController.refresh(req);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assert response.getBody() != null;
    assertEquals("new-at", response.getBody().accessToken());
    assertEquals("new-rt", response.getBody().refreshToken());
  }

  @Test
  void logout_returnsNoContent() {
    LogoutReq req = new LogoutReq("valid-rt");

    ResponseEntity<Void> response = authController.logout("Bearer token-xyz", req);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(authService).logout("Bearer token-xyz", req);
  }
}
