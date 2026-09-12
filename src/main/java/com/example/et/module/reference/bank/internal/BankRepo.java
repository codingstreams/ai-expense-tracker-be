package com.example.et.module.reference.bank.internal;

import com.example.et.module.reference.bank.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface BankRepo extends JpaRepository<Bank, UUID> {
  Long countByIdIn(Set<UUID> bankIds);
}
