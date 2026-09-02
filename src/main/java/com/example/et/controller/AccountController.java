package com.example.et.controller;


import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UpdateCashDto;
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

  @GetMapping(version = "2")
  public ResponseEntity<List<AccountDto>> getUserAccountsV2(@AuthenticationPrincipal String userId, @RequestParam(required = false) String paymentMode) {
    final var accounts = accountService.getUserAccountsV2(userId, paymentMode);
    return ResponseEntity.ok(accounts);
  }

  @GetMapping("/cash")
  public ResponseEntity<AccountDto> getUserCashAccountDetails(@AuthenticationPrincipal String userId) {
    final var account = accountService.getUserCashAccountDetails(userId);
    return ResponseEntity.ok(account);
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

  @PutMapping("/{accountId}")
  public ResponseEntity<AccountDto> updateAccount(@AuthenticationPrincipal String userId, @PathVariable String accountId, @RequestBody AccountDto accountDto) {
    final var account = accountService.updateAccount(userId, accountId, accountDto);
    return ResponseEntity.ok(account);
  }


  @DeleteMapping("/{accountId}")
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal String userId, @PathVariable String accountId) {
    accountService.deleteAccount(userId, accountId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/cash")
  public ResponseEntity<AccountDto> updateCashBalance(@AuthenticationPrincipal String userId, @RequestBody UpdateCashDto requestBody) {
    final var account = accountService.updateCashBalance(userId, requestBody);
    return ResponseEntity.ok(account);
  }
}
