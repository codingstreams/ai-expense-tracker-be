package com.example.et.repo;

import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.model.core.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepo extends JpaRepository<AppUser, UUID> {
  boolean existsByEmail(String email);

  Optional<AppUser> findByEmail(String email);

  boolean existsByEmailAndIsOnboardingComplete(String email, @Param("isOnboardingComplete") boolean onboarded);

  @Query("""
      select new com.example.et.controller.dto.UserDetailsDto(
          u.email,
          u.name,
          u.isOnboardingComplete,
          c.languagePreference,
          c.spendLimit,
          c.currency
      )
      from AppUserConfig c
      right join c.appUser u
      where u.id = :userId
      """)
  UserDetailsDto findByIdWithUserConfig(UUID userId);
}
