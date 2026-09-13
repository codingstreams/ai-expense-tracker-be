package com.example.et.module.reference.paymentmode.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.reference.paymentmode.PaymentModeService;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentModeServiceImpl implements PaymentModeService {
  private final PaymentModeRepo paymentModeRepo;
  private final PaymentModeMapper paymentModeMapper;

  @Override
  @Cacheable(value = CacheNames.PAYMENT_MODES)
  public List<PaymentModeDto> getAllPaymentModes() {
    return paymentModeRepo.findAll()
        .stream()
        .map(paymentModeMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = CacheNames.PAYMENT_MODES, key = "#id", condition = "#id != null")
  public PaymentModeDto getPaymentModeById(UUID id) {
    return paymentModeRepo.findById(id)
        .map(paymentModeMapper::toDto)
        .orElseThrow(() -> new RuntimeException("PaymentMode ID: %s not found".formatted(id)));
  }
}
