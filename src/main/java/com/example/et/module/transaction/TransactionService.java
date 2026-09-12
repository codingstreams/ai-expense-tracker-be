package com.example.et.module.transaction;

import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import com.example.et.module.transaction.dto.TransactionRequestDto;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
  PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable);

  TransactionResponseDto createTransaction(String userId, TransactionRequestDto requestBody);

  void deleteTransaction(String userId, UUID transactionId);

  List<TransactionResponseDto> getRecentTransactions(String userId);

}
