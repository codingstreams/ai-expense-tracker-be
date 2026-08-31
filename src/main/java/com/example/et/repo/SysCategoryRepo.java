package com.example.et.repo;

import com.example.et.model.core.SystemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SysCategoryRepo extends JpaRepository<SystemCategory, UUID> {
}
