package com.example.et.service.paymentmode;

import com.example.et.config.CacheConfig;
import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.mapper.PaymentModeMapper;
import com.example.et.repo.PaymentModeRepo;
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
  @Cacheable(value = CacheConfig.PAYMENT_MODES_CACHE)
  public List<PaymentModeDto> getAllPaymentModes() {
    return paymentModeRepo.findAll()
        .stream()
        .map(paymentModeMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = CacheConfig.PAYMENT_MODES_CACHE, key = "#id", condition = "#id != null")
  public PaymentModeDto getPaymentModeById(UUID id) {
    return paymentModeRepo.findById(id)
        .map(paymentModeMapper::toDto)
        .orElseThrow(() -> new RuntimeException("PaymentMode ID: %s not found".formatted(id)));
  }
}
