package com.example.et.service.appuser;

import com.example.et.controller.dto.UpdateUserDetailsDto;
import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.model.core.AppUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.List;

public interface AppUserService extends UserDetailsService {

  boolean checkUserExists(String email);

  AppUser saveUser(AppUser newUser);

  AppUser getUserByEmail(String email);

  boolean checkIsUserOnboardedByEmail(String userId);

  AppUser getUserByUserId(String userId);

  UserDetailsDto getUserByUserIdWithConfig(String userId);

  UpdateUserDetailsDto updateUserConfig(String userId, UpdateUserDetailsDto userDetailsDto);

  List<AppUser> saveUsers(ArrayList<AppUser> users);
}
