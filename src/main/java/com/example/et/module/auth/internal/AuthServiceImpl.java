package com.example.et.module.auth.internal;

import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.auth.AuthService;
import com.example.et.module.auth.dto.*;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.reference.paymentmode.PaymentModeService;
import com.example.et.module.user.AppUser;
import com.example.et.module.user.AppUserConfig;
import com.example.et.module.user.AppUserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
  private final AppUserService appUserService;
  private final AuthenticationManager authenticationManager;
  private final SecretKey secretKey;
  private final JwtProps jwtProps;
  private final PasswordEncoder passwordEncoder;
  private final BlacklistTokenRepository blacklistTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AccountService accountService;
  private final PaymentModeService paymentModeService;
  private final PaymentModeMapper paymentModeMapper;

  @Transactional
  @Override
  public AuthSuccessResponse register(RegisterUserRequest request) {
    final var userExists = appUserService.checkUserExists(request.email());

    if (userExists) {
      log.info("User {} already exists", request.email());
      throw new RuntimeException("User " + request.email() + " already exists");
    }

    final var cashPaymentMode = paymentModeService.getAllPaymentModes()
        .stream()
        .filter(e -> e.name().equalsIgnoreCase("cash"))
        .findFirst()
        .orElse(null);

    final var newUser = AppUser.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .onboardingComplete(false)
        .build();

    final var userConfig = AppUserConfig.builder()
        .appUser(newUser)
        .languagePreference(AppUserConfig.LanguagePreference.EN)
        .currency(AppUserConfig.Currency.INR)
        .spendLimit(0)
        .paymentMode(paymentModeMapper.toEntity(cashPaymentMode))
        .build();

    newUser.setAppUserConfig(userConfig);

    final var registeredUser = appUserService.saveUser(newUser);

    final var cashAccount = Account.builder()
        .appUser(registeredUser)
        .accountType(Account.AccountType.CASH)
        .balance(0.0f)
        .lastFourDigits("CASH")
        .upiEnabled(false)
        .netBankingEnabled(false)
        .isActive(true)
        .build();

    accountService.saveAccount(cashAccount);

    return login(new LoginRequest(request.email(), request.password()));
  }

  @Override
  public AuthSuccessResponse login(LoginRequest request) {
    final var unauthenticatedToken = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
        request.password());

    final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

    final var userId = ((UserDetails) Objects.requireNonNull(authenticatedToken.getPrincipal())).getUsername();

    final Collection<? extends GrantedAuthority> roles = authenticatedToken.getAuthorities().stream().filter(r -> Objects.requireNonNull(r.getAuthority()).equals("ROLE_USER")).toList();

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var accessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);

    final var expirationTimeRefreshToken = jwtProps.getExpirationTimeRefreshTokenInSeconds();
    final var refreshToken = JwtUtils.generateRefreshToken(userId, secretKey, expirationTimeRefreshToken);

    refreshTokenRepository.saveRefreshToken(userId, refreshToken, Duration.ofSeconds(expirationTimeRefreshToken));

    final var onboarded = appUserService.checkIsUserOnboardedByEmail(request.email());

    final var adjustedExpirationTimeAccessToken = jwtProps.getAdjustedExpirationTimeAccessTokenInSeconds();
    final var expireTime = Instant.now().plusSeconds(adjustedExpirationTimeAccessToken).toEpochMilli();

    return new AuthSuccessResponse(
        accessToken,
        refreshToken,
        BearerAuthToken.TOKEN_TYPE,
        expireTime,
        adjustedExpirationTimeAccessToken,
        onboarded
    );
  }

  @Override
  public AuthSuccessResponse refreshToken(RefreshTokenRequest request) {
    if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
      throw new BadCredentialsException("Refresh token must not be blank");
    }

    final var refreshToken = request.refreshToken();

    final Claims claims;
    try {
      claims = JwtUtils.parseToken(refreshToken, secretKey);
    } catch (Exception e) {
      log.error("Invalid or expired refresh token: {}", e.getMessage());
      throw new BadCredentialsException("Invalid or expired refresh token");
    }

    final var userId = claims.getSubject();
    if (userId == null || !refreshTokenRepository.isRefreshTokenValid(userId, refreshToken)) {
      log.warn("Refresh token reuse or revocation detected for user: {}", userId);
      if (userId != null) {
        refreshTokenRepository.deleteRefreshToken(userId);
      }
      throw new BadCredentialsException("Refresh token is invalid or revoked");
    }

    final var appUser = appUserService.getUserById(userId);
    final Collection<? extends GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var newAccessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);

    final var expirationTimeRefreshToken = jwtProps.getExpirationTimeRefreshTokenInSeconds();
    final var newRefreshToken = JwtUtils.generateRefreshToken(userId, secretKey, expirationTimeRefreshToken);

    refreshTokenRepository.saveRefreshToken(userId, newRefreshToken, Duration.ofSeconds(expirationTimeRefreshToken));

    final var onboarded = appUser.isOnboardingComplete();

    final var adjustedExpirationTimeAccessToken = jwtProps.getAdjustedExpirationTimeAccessTokenInSeconds();
    final var expireTime = Instant.now().plusSeconds(adjustedExpirationTimeAccessToken).toEpochMilli();

    return new AuthSuccessResponse(
        newAccessToken,
        newRefreshToken,
        BearerAuthToken.TOKEN_TYPE,
        expireTime,
        adjustedExpirationTimeAccessToken,
        onboarded
    );
  }

  @Override
  public void logout(String token, LogoutRequest logoutRequest) {
    final var rawTokenOpt = JwtAuthFilter.extractToken(token);
    if (rawTokenOpt.isEmpty()) {
      return;
    }

    final var rawToken = rawTokenOpt.get();
    try {
      final var claims = JwtUtils.getClaimsFromToken(rawToken, secretKey);
      final var expiration = claims.getExpiration();
      final var now = new Date();

      if (expiration != null && expiration.after(now)) {
        long remainingMillis = expiration.getTime() - now.getTime();
        blacklistTokenRepository.add(rawToken, Duration.ofMillis(remainingMillis));
      } else {
        blacklistTokenRepository.add(rawToken);
      }

      final var userId = claims.getSubject();
      if (userId != null) {
        refreshTokenRepository.deleteRefreshToken(userId);
      }
    } catch (Exception e) {
      log.warn("Error parsing claims during logout, blacklisting raw token with default TTL: {}", e.getMessage());
      blacklistTokenRepository.add(rawToken);
    }

    if (logoutRequest != null && logoutRequest.refreshToken() != null) {
      try {
        final var rtClaims = JwtUtils.getClaimsFromToken(logoutRequest.refreshToken(), secretKey);
        final var rtUser = rtClaims.getSubject();
        if (rtUser != null) {
          refreshTokenRepository.deleteRefreshToken(rtUser);
        }
      } catch (Exception e) {
        log.warn("Error parsing refresh token on logout: {}", e.getMessage());
      }
    }
  }
}
