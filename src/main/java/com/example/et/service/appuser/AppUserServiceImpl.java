package com.example.et.service.appuser;

import com.example.et.controller.dto.UpdateUserDetailsDto;
import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.AppUserConfig;
import com.example.et.repo.AppUserConfigRepo;
import com.example.et.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
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
    return appUserRepo.existsByEmailAndIsOnboardingComplete(email, true);
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

    appUserConfigRepo.save(userConfig);

    return userDetailsDto;
  }

  @Override
  public AppUser getUserByEmail(String email) {
    return appUserRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Username: %s not found.".formatted(email)));
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    final var appUser = appUserRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Email %s not found.".formatted(email)));

    return new User(appUser.getId().toString(), appUser.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
