package com.example.et.service.appuser;

import com.example.et.model.core.AppUser;
import com.example.et.repo.AppUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
