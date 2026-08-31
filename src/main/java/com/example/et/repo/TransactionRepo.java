package com.example.et.repo;

import com.example.et.model.core.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
}
