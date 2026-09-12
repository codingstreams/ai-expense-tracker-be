package com.example.et.module.user.internal;

import com.example.et.module.auth.AppUser;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.user.AppUserConfigRepo;
import com.example.et.module.user.AppUserMapper;
import com.example.et.module.user.AppUserRepo;
import com.example.et.module.user.AppUserService;
import com.example.et.module.user.dto.AppUserDto;
import com.example.et.module.user.dto.UpdateUserConfigReq;
import com.example.et.module.user.dto.UpdateUserDetailsDto;
import com.example.et.module.user.dto.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
  private final AppUserRepo appUserRepo;
  private final AppUserConfigRepo appUserConfigRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final AppUserMapper appUserMapper;

  @Override
  public boolean checkUserExists(String email) {
    return appUserRepo.existsByEmail(email);
  }

  @Override
  public AppUser saveUser(AppUser newUser) {
    return appUserRepo.save(newUser);
  }

  @Override
  public boolean checkIsUserOnboardedByEmail(String email) {
    return appUserRepo.existsByEmailAndOnboardingComplete(email, true);
  }

  @Override
  public UserDetailsDto getUserByUserIdWithConfig(String userId) {
    return appUserRepo.findByIdWithUserConfig(UUID.fromString(userId));
  }

  @Override
  public UpdateUserDetailsDto updateUserConfig(String userId, UpdateUserDetailsDto userDetailsDto) {
    final var userConfig = appUserConfigRepo.findByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("User Id: %s not found.".formatted(userId)));

    if (userDetailsDto.currency() != null) {
      userConfig.setCurrency(userDetailsDto.currency());
    }

    if (userDetailsDto.languagePreference() != null) {
      userConfig.setLanguagePreference(userDetailsDto.languagePreference());
    }

    if (userDetailsDto.spendLimit() != null) {
      userConfig.setSpendLimit(userDetailsDto.spendLimit());
    }

    if (userDetailsDto.isOnboardingComplete() != null) {
      userConfig.getAppUser().setOnboardingComplete(userDetailsDto.isOnboardingComplete());
    }

    // Check for payment mode
    final var paymentMode = paymentModeRepo.findByNameIgnoreCase(userDetailsDto.paymentMode())
        .orElseThrow(() -> new RuntimeException("PaymentMode: %s not found.".formatted(userDetailsDto.paymentMode())));

    userConfig.setPaymentMode(paymentMode);

    appUserConfigRepo.save(userConfig);

    return userDetailsDto;
  }

  @Override
  public AppUser getUserByEmail(String email) {
    return appUserRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Username: %s not found.".formatted(email)));
  }

  @Override
  public AppUser getUserById(String userId) {
    return appUserRepo.findById(UUID.fromString(userId))
        .orElseThrow(() -> new UsernameNotFoundException("User Id: %s not found.".formatted(userId)));
  }

  @Override
  @Cacheable(value = "appUserDetails", key = "#userId")
  public AppUserDto getUserByUserIdWithConfigV2(String userId) {
    final var appUser = appUserRepo.findById(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("User Id: %s not found.".formatted(userId)));

    return appUserMapper.toDto(appUser);
  }

  @Override
  @CachePut(value = "appUserDetails", key = "#userId")
  public AppUserDto updateUserConfigV2(String userId, UpdateUserConfigReq userDetailsDto) {
    final var appUser = appUserRepo.findById(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("User Id: %s not found.".formatted(userId)));

    final var appUserConfig = appUser.getAppUserConfig();

    if (userDetailsDto.currency() != null) {
      appUserConfig.setCurrency(userDetailsDto.currency());
    }

    if (userDetailsDto.languagePreference() != null) {
      appUserConfig.setLanguagePreference(userDetailsDto.languagePreference());
    }

    if (userDetailsDto.spendLimit() != null) {
      appUserConfig.setSpendLimit(userDetailsDto.spendLimit());
    }

    if (userDetailsDto.isOnboardingComplete() != null) {
      appUserConfig.getAppUser().setOnboardingComplete(userDetailsDto.isOnboardingComplete());
    }

    // Check for payment mode
    final var paymentMode = paymentModeRepo.findById(UUID.fromString(userDetailsDto.paymentModeId()))
        .orElseThrow(() -> new RuntimeException("PaymentMode: %s not found.".formatted(userDetailsDto.paymentModeId())));

    appUserConfig.setPaymentMode(paymentMode);
    return appUserMapper.toDto(appUserRepo.save(appUser));
  }

  @Override
  public @NonNull UserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
    AppUser appUser = null;
    try {
      final var uuid = UUID.fromString(identifier);
      appUser = appUserRepo.findById(uuid).orElse(null);
    } catch (IllegalArgumentException ignored) {
      // not a UUID, proceed with email
    }

    if (appUser == null) {
      appUser = appUserRepo.findByEmail(identifier)
          .orElseThrow(() -> new UsernameNotFoundException("User %s not found.".formatted(identifier)));
    }

    return new User(appUser.getId().toString(), appUser.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
