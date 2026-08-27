package com.example.et.repo;

import com.example.et.model.core.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
  Page<Transaction> findByAppUserId(UUID appUserId, Pageable pageable);

  List<Transaction> findByAppUserIdOrderByTransactionDateDesc(UUID appUserId, Pageable pageable);

  List<Transaction> findByAppUserIdAndTransactionDateBetween(UUID appUserId, LocalDate startDate, LocalDate endDate);

  Optional<Transaction> findByIdAndAppUserId(UUID id, UUID appUserId);

  List<Transaction> findAllByTransferIdAndAppUserId(UUID transferId, UUID appUserId);

  void deleteByIdAndAppUserId(UUID id, UUID appUserId);
}
