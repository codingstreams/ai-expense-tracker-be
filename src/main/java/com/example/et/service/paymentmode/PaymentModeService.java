package com.example.et.service.paymentmode;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;

import java.util.List;
import java.util.UUID;

public interface PaymentModeService {
  List<PaymentModeDto> getAllPaymentModes();
  PaymentModeDto getPaymentModeById(UUID id);
}
