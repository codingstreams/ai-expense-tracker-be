package com.example.et.controller;


import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping
  public ResponseEntity<List<AccountDto>> addAccounts(@AuthenticationPrincipal String userId, @RequestBody UserBankAccounts accounts) {
    final var createdAccounts = accountService.addAccounts(userId, accounts);
    return ResponseEntity.ok(createdAccounts);
  }

  @GetMapping("/{accountId}")
  public ResponseEntity<AccountDto> getUserAccountDetails(@AuthenticationPrincipal String userId, @PathVariable String accountId) {
    final var account = accountService.getUserAccountDetails(userId, accountId);
    return ResponseEntity.ok(account);
  }
}
