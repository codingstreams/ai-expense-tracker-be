package com.example.et.repo;

import aj.org.objectweb.asm.commons.InstructionAdapter;
import com.example.et.model.core.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentModeRepo extends JpaRepository<PaymentMode, UUID> {
  Optional<PaymentMode> findByNameIgnoreCase(String name);
}
