package com.example.et.service.paymentmode;

import com.example.et.model.core.PaymentMode;

import java.util.List;
import java.util.UUID;

public interface PaymentModeService {
  List<PaymentMode> getAllPaymentModes();
  PaymentMode getPaymentModeById(UUID id);
}
