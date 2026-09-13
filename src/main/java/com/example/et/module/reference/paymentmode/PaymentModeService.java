package com.example.et.module.reference.paymentmode;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentModeService {
  List<PaymentModeDetailsResponse> getAllPaymentModes();

  PaymentModeDetailsResponse getPaymentModeById(UUID id);
}
