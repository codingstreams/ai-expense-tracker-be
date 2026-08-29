package com.example.et.service.auth;

import com.example.et.controller.dto.AuthResponse;
import com.example.et.controller.dto.UserRegistrationRequest;
import com.example.et.model.core.AppUser;
import com.example.et.service.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
  private final AppUserService appUserService;

  @Override
  public AuthResponse register(UserRegistrationRequest request) {
    final var userExists = appUserService.checkUserExists(request.email());

    if (userExists) {
      log.info("User {} already exists", request.email());
      throw new RuntimeException("User " + request.email() + " already exists");
    }

    final var newUser = AppUser.builder()
        .name(request.name())
        .email(request.email())
        .password(request.password())
        .isOnboardingComplete(false)
        .build();

    final var registeredUser = appUserService.saveUser(newUser);

    return null;
  }
}
