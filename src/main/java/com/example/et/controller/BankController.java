package com.example.et.controller;

import com.example.et.controller.dto.bank.BankDto;
import com.example.et.model.core.Bank;
import com.example.et.repo.BankRepo;
import com.example.et.service.bank.BankService;
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
  private final BankService bankService;

  @GetMapping
  public ResponseEntity<List<BankDto>> getBanks() {
    return ResponseEntity.ok().body(bankService.getSupportedBanks());
  }
}
