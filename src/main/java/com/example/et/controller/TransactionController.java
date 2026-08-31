package com.example.et.controller;


import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionFilterParams;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.controller.dto.TransactionResponseDto;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
  private final TransactionService transactionService;

  @GetMapping
  public ResponseEntity<PagedTransactionsDto> getAllTransactions(@AuthenticationPrincipal String userId, @ModelAttribute TransactionFilterParams filterParams, Pageable pageable) {
    return ResponseEntity.ok(transactionService.getAllTransactions(userId, filterParams, pageable));
  }

  @PostMapping
  public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody TransactionRequestDto requestBody, @AuthenticationPrincipal String userId) {
    return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(userId, requestBody));
  }
}
