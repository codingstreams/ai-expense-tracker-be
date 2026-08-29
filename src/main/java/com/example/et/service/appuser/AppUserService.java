package com.example.et.service.appuser;

import com.example.et.model.core.AppUser;
import org.springframework.data.jpa.repository.Query;

public interface AppUserService {
  boolean checkUserExists(String email);

  AppUser saveUser(AppUser newUser);
}
