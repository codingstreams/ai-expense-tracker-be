package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsRequest;
import com.example.et.module.account.dto.UpdateCashBalanceRequest;
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
  public ResponseEntity<List<AccountDetailsResponse>> getUserAccounts(@AuthenticationPrincipal String userId) {
    final var accounts = accountService.getUserAccounts(userId);
    return ResponseEntity.ok(accounts);
  }

  @PostMapping
  public ResponseEntity<List<AccountDetailsResponse>> addAccounts(@AuthenticationPrincipal String userId, @RequestBody CreateAccountsRequest accounts) {
    final var createdAccounts = accountService.addAccounts(userId, accounts);
    return ResponseEntity.ok(createdAccounts);
  }

  @GetMapping("/{accountId}")
  public ResponseEntity<AccountDetailsResponse> getUserAccountDetails(@AuthenticationPrincipal String userId, @PathVariable String accountId) {
    final var account = accountService.getUserAccountDetails(userId, accountId);
    return ResponseEntity.ok(account);
  }

  @GetMapping("/cash")
  public ResponseEntity<AccountDtoOld> getUserCashAccountDetails(@AuthenticationPrincipal String userId) {
    final var account = accountService.getUserCashAccountDetails(userId);
    return ResponseEntity.ok(account);
  }

  @PutMapping("/cash")
  public ResponseEntity<AccountDetailsResponse> updateCashBalance(@AuthenticationPrincipal String userId, @RequestBody UpdateCashBalanceRequest requestBody) {
    final var account = accountService.updateCashBalance(userId, requestBody);
    return ResponseEntity.ok(account);
  }

  @PutMapping("/{accountId}")
  public ResponseEntity<AccountDetailsResponse> updateAccount(@AuthenticationPrincipal String userId, @PathVariable String accountId, @RequestBody AccountDetailsResponse accountDetailsResponse) {
    final var account = accountService.updateAccount(userId, accountId, accountDetailsResponse);
    return ResponseEntity.ok(account);
  }

  @DeleteMapping("/{accountId}")
  public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal String userId, @PathVariable String accountId) {
    accountService.deleteAccount(userId, accountId);
    return ResponseEntity.noContent().build();
  }
}
