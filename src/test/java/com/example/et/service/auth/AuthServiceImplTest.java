package com.example.et.service.auth;

import com.example.et.config.props.JwtProps;
import com.example.et.controller.dto.auth.AuthResponse;
import com.example.et.controller.dto.auth.LoginReq;
import com.example.et.controller.dto.auth.LogoutReq;
import com.example.et.controller.dto.auth.RefreshTokenReq;
import com.example.et.model.core.AppUser;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.util.JwtUtils;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock
  private AppUserService appUserService;
  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private ExpireTokenService expireTokenService;
  @Mock
  private RefreshTokenService refreshTokenService;
  @Mock
  private AccountService accountService;
  @Mock
  private PaymentModeRepo paymentModeRepo;

  private SecretKey secretKey;
  private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    String testKey = "1234567890123456789012345678901234567890";
    secretKey = Keys.hmacShaKeyFor(testKey.getBytes(StandardCharsets.UTF_8));
    JwtProps jwtProps = new JwtProps();
    jwtProps.setSecretKey(testKey);
    jwtProps.setExpirationTimeAccessTokenInMinutes(10);
    jwtProps.setExpirationTimeRefreshTokenInDays(7);

    authService = new AuthServiceImpl(
        appUserService,
        authenticationManager,
        secretKey,
        jwtProps,
        passwordEncoder,
        expireTokenService,
        refreshTokenService,
        accountService,
        paymentModeRepo
    );
  }

  @Test
  void login_success_generatesBothTokensAndStoresRefreshTokenInRedis() {
    UserDetails userDetails = new User("test@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    var authenticatedToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    when(authenticationManager.authenticate(any())).thenReturn(authenticatedToken);
    when(appUserService.checkIsUserOnboardedByEmail("test@example.com")).thenReturn(true);

    AuthResponse response = authService.login(new LoginReq("test@example.com", "pass"));

    assertNotNull(response);
    assertNotNull(response.accessToken());
    assertNotNull(response.refreshToken());
    assertEquals("Bearer", response.tokenType());
    assertTrue(response.onboarded());
    assertTrue(response.expireTime() > System.currentTimeMillis());
    assertEquals(540, response.expiresInSeconds());

    verify(refreshTokenService).saveRefreshToken(eq("test@example.com"), eq(response.refreshToken()), any(Duration.class));
  }

  @Test
  void refreshToken_success_rotatesTokens() {
    String username = "test@example.com";
    String existingRefreshToken = JwtUtils.generateRefreshToken(username, secretKey, 3600);

    when(refreshTokenService.isRefreshTokenValid(username, existingRefreshToken)).thenReturn(true);
    AppUser appUser = AppUser.builder().id(UUID.randomUUID()).email(username).onboardingComplete(true).build();
    when(appUserService.getUserById(username)).thenReturn(appUser);

    AuthResponse response = authService.refreshToken(new RefreshTokenReq(existingRefreshToken));

    assertNotNull(response);
    assertNotNull(response.accessToken());
    assertNotNull(response.refreshToken());
    assertNotEquals(existingRefreshToken, response.refreshToken());
    assertTrue(response.expireTime() > System.currentTimeMillis());
    assertEquals(540, response.expiresInSeconds());

    verify(refreshTokenService).saveRefreshToken(eq(username), eq(response.refreshToken()), any(Duration.class));
  }

  @Test
  void refreshToken_blankToken_throwsBadCredentialsException() {
    assertThrows(BadCredentialsException.class, () -> authService.refreshToken(new RefreshTokenReq("")));
  }

  @Test
  void refreshToken_revokedOrInvalidInRedis_deletesTokenAndThrowsException() {
    String username = "test@example.com";
    String existingRefreshToken = JwtUtils.generateRefreshToken(username, secretKey, 3600);

    when(refreshTokenService.isRefreshTokenValid(username, existingRefreshToken)).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.refreshToken(new RefreshTokenReq(existingRefreshToken)));

    verify(refreshTokenService).deleteRefreshToken(username);
  }

  @Test
  void logout_blacklistsAccessTokenAndDeletesRefreshToken() {
    String username = "test@example.com";
    String accessToken = JwtUtils.generateAccessToken(username, List.of(new SimpleGrantedAuthority("ROLE_USER")), secretKey, 600);
    String header = "Bearer " + accessToken;

    authService.logout(header, new LogoutReq("some-refresh-token"));

    verify(expireTokenService).addExpireToken(eq(accessToken), any(Duration.class));
    verify(refreshTokenService).deleteRefreshToken(username);
  }
}
