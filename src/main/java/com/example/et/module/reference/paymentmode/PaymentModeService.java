package com.example.et.module.reference.paymentmode;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;

import java.util.List;
import java.util.UUID;

public interface PaymentModeService {
  List<PaymentModeDto> getAllPaymentModes();

  PaymentModeDto getPaymentModeById(UUID id);
}
