package com.example.et.repo;

import com.example.et.model.core.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepo extends JpaRepository<AppUser, UUID> {
  boolean existsByEmail(String email);

  Optional<AppUser> findByEmail(String email);

  boolean existsByEmailAndIsOnboardingComplete(String email, boolean isOnboardingComplete);
}
