package com.example.et.service.transaction.strategy;

import com.example.et.model.core.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransactionStrategyFactory {

  private final Map<Transaction.TransactionType, TransactionStrategy> strategies;

  @Autowired
  public TransactionStrategyFactory(List<TransactionStrategy> strategyList) {
    this.strategies = strategyList.stream()
        .collect(Collectors.toUnmodifiableMap(
            TransactionStrategy::getType,
            Function.identity()
        ));
  }

  public TransactionStrategy getTransactionStrategy(Transaction.TransactionType transactionType) {
    return Optional.ofNullable(strategies.get(transactionType))
        .orElseThrow(() -> new IllegalArgumentException(
            "No strategy registered for transaction type: " + transactionType
        ));
  }
}
