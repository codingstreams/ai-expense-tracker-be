package com.example.et.service.paymentmode;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.mapper.PaymentModeMapper;
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
  private final PaymentModeMapper paymentModeMapper;

  @Override
  @Cacheable("paymentModes")
  public List<PaymentModeDto> getAllPaymentModes() {
    return paymentModeRepo.findAll()
        .parallelStream()
        .map(paymentModeMapper::toDto)
        .toList();
  }

  @Override
  @Cacheable(value = "paymentModes", key = "#id")
  public PaymentModeDto getPaymentModeById(UUID id) {
    return paymentModeRepo.findById(id)
        .map(paymentModeMapper::toDto)
        .orElseThrow(() -> new RuntimeException("PaymentMode ID: %s not found".formatted(id)));
  }
}
