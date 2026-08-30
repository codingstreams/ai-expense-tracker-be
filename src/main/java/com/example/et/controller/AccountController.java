package com.example.et.controller;


import com.example.et.controller.dto.AccountDto;
import com.example.et.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
  private final AccountService accountService;

  @GetMapping
  public ResponseEntity<List<AccountDto>> getUserAccounts(@AuthenticationPrincipal String userId) {
    final var accounts = accountService.getUserAccounts(userId);
    return ResponseEntity.ok(accounts);
  }
}
