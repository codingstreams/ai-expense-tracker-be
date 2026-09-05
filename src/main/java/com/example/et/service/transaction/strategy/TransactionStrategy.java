package com.example.et.service.transaction.strategy;

import com.example.et.controller.dto.transaction.TransactionResponseDto;
import com.example.et.model.core.Transaction;
import com.example.et.service.transaction.TransactionContext;

public interface TransactionStrategy {
  TransactionResponseDto execute(TransactionContext transactionContext);
  void delete(String userId, Transaction transaction);
  Transaction.TransactionType getType();
}
