package com.example.et.service.appuser;

import com.example.et.model.core.AppUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AppUserService extends UserDetailsService {

  boolean checkUserExists(String email);

  AppUser saveUser(AppUser newUser);

  AppUser getUserByEmail(String email);

  boolean checkIsUserOnboardedByEmail(String email);

  AppUser getUserByUserId(String userId);
}
