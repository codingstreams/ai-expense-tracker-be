package com.example.et.service.auth;

import com.example.et.config.props.JwtProps;
import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.LoginRequest;
import com.example.et.controller.dto.UserRegistrationRequest;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.AppUserConfig;
import com.example.et.model.core.LanguagePreference;
import com.example.et.security.BearerAuthToken;
import com.example.et.security.JwtAuthFilter;
import com.example.et.service.appuser.AppUserService;
import com.example.et.service.exception.UserAlreadyExistsException;
import com.example.et.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final AppUserService appUserService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final SecretKey secretKey;
  private final JwtProps jwtProps;
  private final ExpireTokenService expireTokenService;

  @Override
  public AuthResponse login(LoginRequest request) {
    // Create unauthenticated email(username) and password token
    final var unauthenticatedToken = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
        request.password());

    // Authenticated Token
    final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

    // Generate Access Token & Refresh Token
    // Extract User ID from authenticated token
    final var userId = ((UserDetails) Objects.requireNonNull(authenticatedToken.getPrincipal())).getUsername(); // User's ID => Subject

    final Collection<? extends GrantedAuthority> roles = authenticatedToken.getAuthorities().stream().filter(r -> Objects.requireNonNull(r.getAuthority()).equals("ROLE_USER")).toList();

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var accessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);

    final var onboarded = appUserService.checkIsUserOnboardedByEmail(userId);

    return new AuthResponse(
        accessToken,
        BearerAuthToken.TOKEN_TYPE,
        expirationTimeAccessToken,
        onboarded
    );
  }

  @Override
  public AuthResponse registerUser(UserRegistrationRequest request) {
    // Check whether user exists or not
    final var userExists = appUserService.checkUserExists(request.email());

    if (userExists) throw new UserAlreadyExistsException("Email/User: %s already present.".formatted(request.email()));

    final var newUser = AppUser.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .build();

    final var userConfig = AppUserConfig.builder()
        .appUser(newUser)
        .languagePreference(LanguagePreference.EN)
        .currency(AppUserConfig.Currency.INR)
        .spendLimit(BigDecimal.ZERO)
        .build();

    newUser.setAppUserConfig(userConfig);

    final var registeredUser = appUserService.saveUser(newUser);

    return login(new LoginRequest(registeredUser.getEmail(), request.password()));
  }

  @Override
  public void logout(String token) {
    JwtAuthFilter.extractToken(token).ifPresent(expireTokenService::addExpireToken);
  }
}
