package com.example.et.module.reference.bank;

import com.example.et.module.reference.bank.dto.BankDetailsResponse;
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
  public ResponseEntity<List<BankDetailsResponse>> getBanks() {
    return ResponseEntity.ok().body(bankService.getSupportedBanks());
  }
}
