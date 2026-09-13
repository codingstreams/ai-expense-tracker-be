package com.example.et.module.transaction;

import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
  PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable);

  TransactionDetailsResponse createTransaction(String userId, CreateTransactionRequest createTransactionRequest);

  void deleteTransaction(String userId, String transactionId);

  List<TransactionDetailsResponse> getRecentTransactions(String userId);

}
