package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionDto;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.controller.dto.TransactionResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransactionsService {
  TransactionDto createTransaction(String userId, TransactionRequestDto dto);

  PagedTransactionsDto getAllTransactions(String userId, Pageable pageable);

  List<TransactionDto> getRecentTransactions(String userId);

  List<TransactionResponseDto> getRecentTransactionsV2(String userId);

  void deleteTransaction(String userId, UUID transactionId);
}
