package com.example.et.module.auth;

import com.example.et.module.account.AccountService;
import com.example.et.module.auth.dto.AuthSuccessResponse;
import com.example.et.module.auth.dto.LoginRequest;
import com.example.et.module.auth.dto.LogoutRequest;
import com.example.et.module.auth.dto.RefreshTokenRequest;
import com.example.et.module.auth.internal.*;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.user.AppUser;
import com.example.et.module.user.AppUserService;
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
  private BlacklistTokenRepository blacklistTokenRepository;
  @Mock
  private RefreshTokenRepository refreshTokenRepository;
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
        blacklistTokenRepository,
        refreshTokenRepository,
        accountService,
        null, null
    );
  }

  @Test
  void login_success_generatesBothTokensAndStoresRefreshTokenInRedis() {
    UserDetails userDetails = new User("test@example.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    var authenticatedToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    when(authenticationManager.authenticate(any())).thenReturn(authenticatedToken);
    when(appUserService.checkIsUserOnboardedByEmail("test@example.com")).thenReturn(true);

    AuthSuccessResponse response = authService.login(new LoginRequest("test@example.com", "pass"));

    assertNotNull(response);
    assertNotNull(response.accessToken());
    assertNotNull(response.refreshToken());
    assertEquals("Bearer", response.tokenType());
    assertTrue(response.onboarded());
    assertTrue(response.expireTime() > System.currentTimeMillis());
    assertEquals(540, response.expiresInSeconds());

    verify(refreshTokenRepository).saveRefreshToken(eq("test@example.com"), eq(response.refreshToken()), any(Duration.class));
  }

  @Test
  void refreshToken_success_rotatesTokens() {
    String username = "test@example.com";
    String existingRefreshToken = JwtUtils.generateRefreshToken(username, secretKey, 3600);

    when(refreshTokenRepository.isRefreshTokenValid(username, existingRefreshToken)).thenReturn(true);
    AppUser appUser = AppUser.builder().id(UUID.randomUUID()).email(username).onboardingComplete(true).build();
    when(appUserService.getUserById(username)).thenReturn(appUser);

    AuthSuccessResponse response = authService.refreshToken(new RefreshTokenRequest(existingRefreshToken));

    assertNotNull(response);
    assertNotNull(response.accessToken());
    assertNotNull(response.refreshToken());
    assertNotEquals(existingRefreshToken, response.refreshToken());
    assertTrue(response.expireTime() > System.currentTimeMillis());
    assertEquals(540, response.expiresInSeconds());

    verify(refreshTokenRepository).saveRefreshToken(eq(username), eq(response.refreshToken()), any(Duration.class));
  }

  @Test
  void refreshToken_blankToken_throwsBadCredentialsException() {
    assertThrows(BadCredentialsException.class, () -> authService.refreshToken(new RefreshTokenRequest("")));
  }

  @Test
  void refreshToken_revokedOrInvalidInRedis_deletesTokenAndThrowsException() {
    String username = "test@example.com";
    String existingRefreshToken = JwtUtils.generateRefreshToken(username, secretKey, 3600);

    when(refreshTokenRepository.isRefreshTokenValid(username, existingRefreshToken)).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.refreshToken(new RefreshTokenRequest(existingRefreshToken)));

    verify(refreshTokenRepository).deleteRefreshToken(username);
  }

  @Test
  void logout_blacklistsAccessTokenAndDeletesRefreshToken() {
    String username = "test@example.com";
    String accessToken = JwtUtils.generateAccessToken(username, List.of(new SimpleGrantedAuthority("ROLE_USER")), secretKey, 600);
    String header = "Bearer " + accessToken;

    authService.logout(header, new LogoutRequest("some-refresh-token"));

    verify(blacklistTokenRepository).add(eq(accessToken), any(Duration.class));
    verify(refreshTokenRepository).deleteRefreshToken(username);
  }
}
