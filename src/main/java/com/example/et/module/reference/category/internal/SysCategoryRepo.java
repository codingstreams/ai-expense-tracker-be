package com.example.et.module.reference.category.internal;

import com.example.et.module.reference.category.SystemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SysCategoryRepo extends JpaRepository<SystemCategory, UUID> {
  Optional<SystemCategory> findFirstByNameContainingIgnoreCase(String name);
}
