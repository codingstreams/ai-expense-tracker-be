package com.example.et.service.transaction;

import com.example.et.controller.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionsService {
  TransactionDto createTransaction(String userId, TransactionRequestDto dto);

  PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable);

  List<TransactionDto> getRecentTransactions(String userId);

  List<TransactionResponseDto> getRecentTransactionsV2(String userId);

  void deleteTransaction(String userId, UUID transactionId);
}
