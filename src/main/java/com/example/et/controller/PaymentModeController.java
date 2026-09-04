package com.example.et.controller;

import com.example.et.model.core.PaymentMode;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.service.paymentmode.PaymentModeService;
import jakarta.persistence.Cacheable;
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
  public ResponseEntity<List<PaymentMode>> getPaymentModes() {
    return ResponseEntity.ok().body(paymentModeService.getAllPaymentModes());
  }
}
