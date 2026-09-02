package com.example.et.repo;

import aj.org.objectweb.asm.commons.InstructionAdapter;
import com.example.et.model.core.SystemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SysCategoryRepo extends JpaRepository<SystemCategory, UUID> {
  Optional<SystemCategory> findFirstByNameContainingIgnoreCase(String name);
}
