package com.example.et.repo;

import com.example.et.model.core.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankRepo extends JpaRepository<Bank, UUID> {
  Optional<Bank> findByName(String name);
}
