package com.example.et.module.transaction;


import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
  private final TransactionService transactionService;

  @GetMapping
  public ResponseEntity<PagedTransactionsDto> getAllTransactions(@AuthenticationPrincipal String userId, @ModelAttribute TransactionFilterParams filterParams, Pageable pageable) {
    return ResponseEntity.ok(transactionService.getAllTransactions(userId, filterParams, pageable));
  }

  @GetMapping("/recent")
  public ResponseEntity<List<TransactionDetailsResponse>> getRecentTransactions(@AuthenticationPrincipal String userId) {
    final var transactions = transactionService.getRecentTransactions(userId);
    return ResponseEntity.ok(transactions);
  }

  @PostMapping
  public ResponseEntity<TransactionDetailsResponse> createTransaction(@RequestBody CreateTransactionRequest requestBody, @AuthenticationPrincipal String userId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(transactionService.createTransaction(userId, requestBody));
  }

  @DeleteMapping("/{transactionId}")
  public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal String userId, @PathVariable String transactionId) {
    transactionService.deleteTransaction(userId, transactionId);
    return ResponseEntity.ok().build();
  }
}
