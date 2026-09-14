package com.example.et.service.auth;

import com.example.et.config.props.JwtProps;
import com.example.et.controller.dto.RefreshTokenReq;
import com.example.et.controller.dto.auth.AuthResponse;
import com.example.et.controller.dto.auth.CreateUserReq;
import com.example.et.controller.dto.auth.LoginReq;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.AppUserConfig;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.security.BearerAuthToken;
import com.example.et.security.JwtAuthFilter;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
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
  private final RefreshTokenService refreshTokenService;

  @Qualifier("redisExpireTokenService")
  private final ExpireTokenService expireTokenService;

  private final AccountService accountService;
  private final PaymentModeRepo paymentModeRepo;

  @Override
  public AuthResponse register(CreateUserReq request) {
    final var userExists = appUserService.checkUserExists(request.email());

    if (userExists) {
      log.info("User {} already exists", request.email());
      throw new RuntimeException("User " + request.email() + " already exists");
    }

    final var cashPaymentMode = paymentModeRepo.findByNameIgnoreCase("Cash").orElse(null);

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
        .paymentMode(cashPaymentMode)
        .build();

    newUser.setAppUserConfig(userConfig);

    appUserService.saveUser(newUser);

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

    return login(new LoginReq(request.email(), request.password()));
  }

  @Override
  public AuthResponse login(LoginReq request) {
    final var unauthenticatedToken = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
        request.password());

    final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

    final var userId = ((UserDetails) Objects.requireNonNull(authenticatedToken.getPrincipal())).getUsername();

    final Collection<? extends GrantedAuthority> roles = authenticatedToken.getAuthorities().stream().filter(r -> Objects.requireNonNull(r.getAuthority()).equals("ROLE_USER")).toList();

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var expirationTimeRefreshToken = jwtProps.getExpirationTimeRefreshTokenInSeconds();

    final var accessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);
    final var refreshToken = JwtUtils.generateRefreshToken(userId, secretKey, expirationTimeRefreshToken);

    final var refreshTokenClaims = JwtUtils.parseToken(refreshToken, secretKey);

    // add this rt to cache
    refreshTokenService.saveRefreshToken(userId, refreshTokenClaims.getId(), Duration.ofSeconds(expirationTimeRefreshToken));

    final var onboarded = appUserService.checkIsUserOnboardedByEmail(request.email());

    return new AuthResponse(
        accessToken,
        BearerAuthToken.TOKEN_TYPE,
        expirationTimeAccessToken,
        onboarded,
        refreshToken
    );
  }

  @Override
  public void logout(String token) {
    // JTI
    final var rawToken = JwtAuthFilter.extractToken(token);

    if (rawToken.isEmpty()) {
      return;
    }

    try {
      final var claims = JwtUtils.getClaimsFromToken(rawToken.get(), secretKey);
      final var exp = claims.getExpiration();
      final var now = new Date();
      final var jti = claims.getId();

      // Calculate TTL
      if (exp != null && !exp.after(now)) {
        final var ttl = exp.getTime() - now.getTime(); // time diff is in ms
        expireTokenService.addExpireToken(jti, Duration.ofMillis(ttl));
      } else {
        expireTokenService.addExpireToken(jti);
      }
    } catch (Exception e) {
      log.warn("Error parsing claims during logout, blacklisting raw token with default TTL: {}", e.getMessage());
      expireTokenService.addExpireToken(rawToken.get());
    }

  }

  @Override
  public AuthResponse refreshToken(RefreshTokenReq refreshTokenReq) {
    final var refreshToken = refreshTokenReq.refreshToken();

    final Claims claims;
    try {
      claims = JwtUtils.parseToken(refreshToken, secretKey);
    } catch (Exception e) {
      log.error("Invalid or expired refresh token: {}", e.getMessage());
      throw new BadCredentialsException("Invalid or expired refresh token");
    }

    final var userId = claims.getSubject();
    final var jti = claims.getId();

    if (userId == null || !refreshTokenService.isRefreshTokenValid(userId, jti)) {
      log.warn("Refresh token reuse or revocation detected for user: {}", userId);
      if (userId != null) {
        refreshTokenService.deleteRefreshToken(userId);
      }
      throw new BadCredentialsException("Refresh token is invalid or revoked");
    }

    final var appUser = appUserService.getUserByUserIdWithConfigV2(userId);
    final Collection<? extends GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ROLE_USER"));

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var newAccessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);

    final var expirationTimeRefreshToken = jwtProps.getExpirationTimeRefreshTokenInSeconds();
    final var newRefreshToken = JwtUtils.generateRefreshToken(userId, secretKey, expirationTimeRefreshToken);
    final var refreshTokenClaims = JwtUtils.parseToken(newRefreshToken, secretKey);

    refreshTokenService.saveRefreshToken(userId, refreshTokenClaims.getId(), Duration.ofSeconds(expirationTimeRefreshToken));

    final var onboarded = appUser.onboardingComplete();

    return new AuthResponse(
        newAccessToken,
        BearerAuthToken.TOKEN_TYPE,
        expirationTimeAccessToken,
        onboarded,
        newRefreshToken
    );
  }
}
