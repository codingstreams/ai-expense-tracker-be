package com.example.et.service.appuser;

import com.example.et.controller.dto.UpdateUserDetailsDto;
import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.model.core.AppUser;
import com.example.et.repo.AppUserConfigRepo;
import com.example.et.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
  public AppUser getUserByEmail(String email) {
    return appUserRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Username: %s not found.".formatted(email)));
  }

  @Override
  public boolean checkIsUserOnboardedByEmail(String email) {
    return appUserRepo.existsByEmailAndIsOnboardingComplete(email, true);
  }

  @Override
  public AppUser getUserByUserId(String userId) {
    return appUserRepo.findById(UUID.fromString(userId))
        .orElseThrow(() -> new UsernameNotFoundException("User Id: %s not found.".formatted(userId)));
  }

  @Override
  public UserDetailsDto getUserByUserIdWithConfig(String userId) {
    return appUserRepo.findByIdWithUserConfig(UUID.fromString(userId));
  }

  @Override
  public UpdateUserDetailsDto updateUserConfig(String userId, UpdateUserDetailsDto userDetailsDto) {
    final var userConfig = appUserConfigRepo.findByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new UsernameNotFoundException("User Id: %s not found.".formatted(userId)));

    userConfig.setCurrency(userDetailsDto.currency());
    userConfig.setLanguagePreference(userDetailsDto.languagePreference());
    userConfig.setSpendLimit(userDetailsDto.spendLimit());

    appUserConfigRepo.save(userConfig);

    return userDetailsDto;
  }

  @Override
  public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
    final var appUser = getUserByEmail(email);
    return new User(appUser.getId().toString(), appUser.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
