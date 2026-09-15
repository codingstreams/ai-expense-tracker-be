package com.example.et.module.transaction.internal;

import com.example.et.module.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  Optional<Transaction> findByIdAndAppUserId(String transactionId, String userId);

  List<Transaction> findAllByTransferIdAndAppUserId(String transferId, String userId);
}
