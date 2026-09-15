package com.example.et.module.reference.paymentmode.internal;

import com.example.et.module.reference.paymentmode.PaymentMode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentModeRepo extends JpaRepository<PaymentMode, UUID> {
  Optional<PaymentMode> findByNameIgnoreCase(String name);
}
