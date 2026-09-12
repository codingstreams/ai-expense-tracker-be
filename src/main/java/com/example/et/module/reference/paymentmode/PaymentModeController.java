package com.example.et.module.reference.paymentmode;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment-modes")
@RequiredArgsConstructor
public class PaymentModeController {
  private final PaymentModeService paymentModeService;

  @GetMapping
  public ResponseEntity<List<PaymentModeDto>> getPaymentModes() {
    return ResponseEntity.ok().body(paymentModeService.getAllPaymentModes());
  }
}
