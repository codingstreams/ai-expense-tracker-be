package com.example.et.service.paymentmode;

import com.example.et.model.core.PaymentMode;
import com.example.et.repo.PaymentModeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentModeServiceImpl implements PaymentModeService {
  private final PaymentModeRepo paymentModeRepo;

  @Override
  @Cacheable("paymentModes")
  public List<PaymentMode> getAllPaymentModes() {
    return paymentModeRepo.findAll();
  }

  @Override
  @Cacheable(value = "paymentModes", key = "#id")
  public PaymentMode getPaymentModeById(UUID id) {
    return paymentModeRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("PaymentMode ID: %s not found".formatted(id)));
  }
}
