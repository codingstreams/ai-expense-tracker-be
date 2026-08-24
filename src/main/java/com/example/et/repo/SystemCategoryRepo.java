package com.example.et.repo;

import com.example.et.model.core.SystemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SystemCategoryRepo extends JpaRepository<SystemCategory, UUID> {
  Optional<SystemCategory> findByNameIgnoreCase(String name);

  Optional<SystemCategory> findFirstByNameContainingIgnoreCase(String name);
}
