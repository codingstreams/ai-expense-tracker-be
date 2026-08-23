package com.example.et.repo;

import com.example.et.model.core.AppUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AppUserConfigRepo extends JpaRepository<AppUserConfig, UUID> {
  @Query("""
    select u
    from AppUserConfig u
    where u.appUser.id = :userId
""")
  Optional<AppUserConfig> findByUserId(UUID userId);
}
