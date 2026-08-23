package com.example.et.controller;

import com.example.et.model.core.Account;
import com.example.et.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
  private final AccountService accountService;

  // List all user accounts with live balances
  @GetMapping
  public ResponseEntity<List<Account>> getUserAccounts(UUID uuid) {
    var accounts = accountService.getUserAccounts(uuid);
    return ResponseEntity.ok(accounts);
  }


}
