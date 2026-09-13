package com.example.et.module.user;

import com.example.et.module.user.dto.AppUserDto;
import com.example.et.module.user.dto.UpdateUserConfigReq;
import com.example.et.module.user.dto.UpdateUserDetailsDto;
import com.example.et.module.user.dto.UserDetailsDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AppUserService extends UserDetailsService {
  boolean checkUserExists(String email);

  AppUser saveUser(AppUser newUser);

  boolean checkIsUserOnboardedByEmail(String email);

  UserDetailsDto getUserByUserIdWithConfig(String userId);

  UpdateUserDetailsDto updateUserConfig(String userId, UpdateUserDetailsDto userDetailsDto);

  AppUser getUserByEmail(String email);

  AppUser getUserById(String userId);

  AppUserDto getUserByUserIdWithConfigV2(String userId);

  AppUserDto updateUserConfigV2(String userId, UpdateUserConfigReq userDetailsDto);
}
