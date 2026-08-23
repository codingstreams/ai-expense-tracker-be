package com.example.et.controller;

import com.example.et.model.core.Bank;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
@RequiredArgsConstructor
public class BankController {
  private final BankRepo bankRepo;

  @GetMapping
  public ResponseEntity<List<Bank>> getPaymentModes() {
    return ResponseEntity.ok().body(bankRepo.findAll());
  }
}
