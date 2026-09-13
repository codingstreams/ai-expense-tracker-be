package com.example.et.module.transaction.internal.strategy;

import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;

public interface TransactionStrategy {
  TransactionDetailsResponse execute(TransactionContext transactionContext);

  void delete(String userId, Transaction transaction);

  Transaction.TransactionType getType();
}
