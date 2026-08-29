package com.example.et.service.appuser;

import com.example.et.model.core.AppUser;
import com.example.et.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
  private final AppUserRepo appUserRepo;

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
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    final var appUser = appUserRepo.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Email %s not found.".formatted(email)));

    return new User(appUser.getId().toString(), appUser.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }
}
