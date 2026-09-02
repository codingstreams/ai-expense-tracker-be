package com.example.et.service.auth;

import com.example.et.config.props.JwtProps;
import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.LoginRequest;
import com.example.et.controller.dto.UserRegistrationRequest;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.AppUserConfig;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.security.BearerAuthToken;
import com.example.et.security.JwtAuthFilter;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
  private final AppUserService appUserService;
  private final AuthenticationManager authenticationManager;
  private final SecretKey secretKey;
  private final JwtProps jwtProps;
  private final PasswordEncoder passwordEncoder;
  private final ExpireTokenService expireTokenService;
  private final AccountService accountService;
  private final PaymentModeRepo paymentModeRepo;

  @Override
  public AuthResponse register(UserRegistrationRequest request) {
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
        .isOnboardingComplete(false)
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
        .isUpiEnabled(false)
        .isNetBankingEnabled(false)
        .isActive(true)
        .build();

    accountService.saveAccount(cashAccount);

    return login(new LoginRequest(request.email(), request.password()));
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    final var unauthenticatedToken = UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
        request.password());

    final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

    final var userId = ((UserDetails) Objects.requireNonNull(authenticatedToken.getPrincipal())).getUsername();

    final Collection<? extends GrantedAuthority> roles = authenticatedToken.getAuthorities().stream().filter(r -> Objects.requireNonNull(r.getAuthority()).equals("ROLE_USER")).toList();

    final var expirationTimeAccessToken = jwtProps.getExpirationTimeAccessTokenInSeconds();
    final var accessToken = JwtUtils.generateAccessToken(userId, roles, secretKey, expirationTimeAccessToken);

    final var onboarded = appUserService.checkIsUserOnboardedByEmail(request.email());

    return new AuthResponse(
        accessToken,
        BearerAuthToken.TOKEN_TYPE,
        expirationTimeAccessToken,
        onboarded
    );
  }

  @Override
  public void logout(String token) {
    JwtAuthFilter.extractToken(token).ifPresent(expireTokenService::addExpireToken);
  }
}
