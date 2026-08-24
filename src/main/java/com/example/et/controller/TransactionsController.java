package com.example.et.controller;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionDto;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.service.transaction.TransactionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {
  private final TransactionsService transactionsService;

  @PostMapping
  public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionRequestDto requestBody, @AuthenticationPrincipal String userId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(transactionsService.createTransaction(userId, requestBody));
  }

  @GetMapping
  public ResponseEntity<PagedTransactionsDto> getAllTransactions(@AuthenticationPrincipal String userId, Pageable pageable) {
    return ResponseEntity.ok(transactionsService.getAllTransactions(userId, pageable));
  }

  @GetMapping("/recent")
  public ResponseEntity<List<TransactionDto>> getRecentTransactions(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(transactionsService.getRecentTransactions(userId));
  }

  @DeleteMapping("/{transactionId}")
  public ResponseEntity<Void> deleteTransaction(@PathVariable UUID transactionId, @AuthenticationPrincipal String userId) {
    transactionsService.deleteTransaction(userId, transactionId);
    return ResponseEntity.ok().build();
  }
}
