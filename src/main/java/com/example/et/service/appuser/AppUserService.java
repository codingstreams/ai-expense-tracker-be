package com.example.et.service.appuser;

import com.example.et.controller.dto.appuser.AppUserDto;
import com.example.et.controller.dto.appuser.UpdateUserConfigReq;
import com.example.et.controller.dto.appuser.UpdateUserDetailsDto;
import com.example.et.controller.dto.appuser.UserDetailsDto;
import com.example.et.model.core.AppUser;
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
