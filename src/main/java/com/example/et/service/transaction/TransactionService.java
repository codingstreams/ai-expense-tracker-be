package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionFilterParams;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.controller.dto.TransactionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
  PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable);

  TransactionResponseDto createTransaction(String userId, TransactionRequestDto requestBody);

  void deleteTransaction(String userId, UUID transactionId);

  List<TransactionResponseDto> getRecentTransactions(String userId);
}
