package com.example.et.service.appuser;

import com.example.et.controller.dto.UpdateUserDetailsDto;
import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.model.core.AppUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AppUserService extends UserDetailsService {
  boolean checkUserExists(String email);

  AppUser saveUser(AppUser newUser);

  boolean checkIsUserOnboardedByEmail(String email);

  UserDetailsDto getUserByUserIdWithConfig(String userId);

  UpdateUserDetailsDto updateUserConfig(String userId, UpdateUserDetailsDto userDetailsDto);

  AppUser getUserByEmail(String email);
}
